package com.childrenofcorn.eldorado

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.jsoup.nodes.Document
import com.childrenofcorn.eldorado.databinding.ActivityListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding

    private lateinit var productsArrayList: ArrayList<Product>
    private lateinit var listAdapter: ListAdapter
    private lateinit var parser: ProductParser

    private lateinit var db: FirebaseFirestore

    private var herokuWebsiteURL = "http://children-of-corn-eldorado.herokuapp.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle("Список товаров")

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        db = Firebase.firestore

        val recViewListOfProducts = binding.listOfProducts

        var recViewDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recViewListOfProducts.addItemDecoration(recViewDecoration)

        parser = ProductParser("")
        productsArrayList = arrayListOf()
        listAdapter = ListAdapter(productsArrayList)

        recViewListOfProducts.adapter = listAdapter
        recViewListOfProducts.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val productToDelete = listAdapter.getItemAtPositition(viewHolder.adapterPosition)
                val productPosition = viewHolder.adapterPosition
                listAdapter.deleteItemAtPosition(viewHolder.adapterPosition)
                if (listAdapter.itemCount == 0) {
                    binding.textViewEmptyScreen.visibility = View.VISIBLE
                }
                Snackbar.make(binding.root, productToDelete.name, Snackbar.LENGTH_LONG)
                    .setAction("Undo", View.OnClickListener {
                        binding.textViewEmptyScreen.visibility = View.GONE
                        productsArrayList.add(productPosition, productToDelete)
                        listAdapter.notifyDataSetChanged()
                    }).show()
            }
        })

        itemTouchHelper.attachToRecyclerView(recViewListOfProducts)

        binding.buttonSend.setOnClickListener {

            if (productsArrayList.size > 0) {
                var alertDialogCodeType = createAlertDialogCodeType()
                alertDialogCodeType.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            val url = data.getStringExtra("ProductLink")
            val parser = ProductParser(url)
            addProductToList(parser, productsArrayList, listAdapter)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_action_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_scan -> {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivityForResult(intent, 0)
            true
        }

        R.id.action_reset -> {
            binding.textViewEmptyScreen.visibility = View.VISIBLE
            productsArrayList.clear()
            listAdapter.notifyDataSetChanged()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun addProductToList(parser: ProductParser,
                                 productsArray: ArrayList<Product>,
                                 listAdapter: ListAdapter) {

        lateinit var document: Document
        lateinit var productImageLink : String
        lateinit var productName: String
        var productPrice: String

        try {
            Thread(Runnable {
                document = parser.getDocument()

                productImageLink = parser.getProductImageLink(document)
                productName = parser.getProductName(document)
                productPrice = parser.getProductPrice(document)

                Log.d("Received product name", productName)
                Log.d("Received product price", productPrice.toString())
                Log.d("Received product image", productImageLink)

                runOnUiThread {
                    binding.textViewEmptyScreen.visibility = View.GONE
                    productsArray.add(
                        Product(
                            productName,
                            productPrice,
                            productImageLink,
                            parser.getProductURL()
                        )
                    )
                    listAdapter.notifyDataSetChanged()
                }
            }).start()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.incorrect_link, Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadListToFirebase(list: ArrayList<Product>,
                                     isAnonymous: Boolean,
                                     clientRef: String) {

        var id: String? = FirebaseAuth.getInstance().currentUser?.uid

        var document = mutableMapOf("datetime" to Calendar.getInstance().time,
            "products" to list,
            "staff_ref" to db.document("staff/$id"))

        /*var document = mutableMapOf("datetime" to Calendar.getInstance().time,
                                "products" to listOfLinks,
                                "staff_ref" to db.document("staff/$id"))*/

        if (!isAnonymous) {
            document["client_ref"] = db.document("clients/$clientRef")
        }

        db.collection("carts").add(document).addOnCompleteListener {
            if (it.isSuccessful) {
                var cartID = it.result?.id
                if (id != null) {
                    db.collection("staff").document(id)
                        .update("refs", FieldValue.arrayUnion(db.document("/carts/$cartID")))
                }
                Toast.makeText(this, R.string.upload_successful, Toast.LENGTH_SHORT).show()

                if (cartID != null) {
                    Log.d("CartID", cartID)
                }

                val queue = Volley.newRequestQueue(this)
                var url = herokuWebsiteURL + "send-email/$cartID"

                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                    },
                    { })

                queue.add(stringRequest)

                val intent = Intent(this, QRActivity::class.java)
                intent.putExtra("URL", herokuWebsiteURL + cartID)
                startActivity(intent)
            }
        }
    }

    fun createAlertDialogUserPhone(): AlertDialog {
        var alertDialogBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_user_phone, null)
        alertDialogBuilder.setView(view)
        alertDialogBuilder.setMessage(R.string.dialog_user_phone_message)
        alertDialogBuilder.setPositiveButton(
            R.string.proceed,
            DialogInterface.OnClickListener { dialog, which ->
                var userPhoneNumber = view.findViewById<EditText>(R.id.edit_text_phone).text.toString()
                if (userPhoneNumber != "") {
                    try {
                        uploadListToFirebase(productsArrayList, false, userPhoneNumber)
                    } catch (e: Exception) {
                        Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
                }
            })
        var alertDialog = alertDialogBuilder.create()
        return alertDialog
    }



    fun createAlertDialogCodeType(): AlertDialog {
        var alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(R.string.dialog_anonymous_list_message)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton(
            R.string.yes,
            DialogInterface.OnClickListener { dialog, which ->
                uploadListToFirebase(
                    productsArrayList,
                    true,
                    ""
                )
            })
        alertDialogBuilder.setNegativeButton(
            R.string.no,
            DialogInterface.OnClickListener { dialog, which ->
                var dialogUserPhone = createAlertDialogUserPhone()
                dialogUserPhone.show()
            })
        var alertDialog = alertDialogBuilder.create()
        return alertDialog
    }

}