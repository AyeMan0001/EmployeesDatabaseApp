package eu.tutorials.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import eu.tutorials.roomdemo.databinding.ActivityMainBinding
import eu.tutorials.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener{
            addRecord(employeeDao)
        }
        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect{
                //make an arraylist out of the "it" list
                val list = ArrayList(it)
                //and then pass it to the setupListDataIntoRecyclerView method
                setupListDataIntoRecyclerView(list,employeeDao)
            }
        }

    }

    private fun addRecord(employeeDao: EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()
        val jobPosition = binding?.etJob?.text.toString()
        val phoneNumber = binding?.etPhone?.text.toString()

        if(name.isNotEmpty() && email.isNotEmpty()){
            lifecycleScope.launch{
                employeeDao.insert(EmployeeEntity(name=name, email = email, jobPosition = jobPosition, phoneNumber = phoneNumber))
                Toast.makeText(applicationContext,"Record saved", Toast.LENGTH_LONG).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
                binding?.etJob?.text?.clear()
                binding?.etPhone?.text?.clear()

            }
        }else{
            Toast.makeText(applicationContext,"Name ,Email ,Job Position or Phone number cannot be blank",
            Toast.LENGTH_LONG).show()
        }


    }


    private fun setupListDataIntoRecyclerView(
        employeesList:ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDao){
        if(employeesList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeesList,{
                updateId ->
                updateRecordDialog(updateId,employeeDao)
            },
                {
                    deleteId ->
                    lifecycleScope.launch {
                        employeeDao.fetchEmployeeById(deleteId).collect {
                            if (it != null) {
                                deleteRecordAlertDialog(deleteId, employeeDao, it)
                            }
                            }
                        }

                    }

                )
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE

        }

    }
  private  fun updateRecordDialog(id:Int, employeeDao: EmployeeDao){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect{
                if(it !=null){
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                    binding.etUpdateJob.setText(it.jobPosition)
                    binding.etUpdatePhone.setText(it.phoneNumber)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()
            val job = binding.etUpdateJob.text.toString()
            val phone = binding.etUpdatePhone.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty() && job.isNotEmpty() && phone.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id,name,email,job,phone))
                    Toast.makeText(applicationContext,"Record Updated.", Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(                    applicationContext,
                    "Name or Email cannot be blank.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        updateDialog.show()

    }


   private fun deleteRecordAlertDialog(id:Int,employeeDao: EmployeeDao,employee: EmployeeEntity) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Record")
        //set message for alert dialog
        builder.setMessage("Are you sure you wants to delete ${employee.name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()

                dialogInterface.dismiss() // Dialog will be dismissed
            }

        }


        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }



}