package com.cashwind.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.cashwind.app.database.entity.BillEntity
import com.cashwind.app.database.entity.BillPaymentAllocationEntity
import com.cashwind.app.util.DateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class PastDueBillsActivity : BaseActivity() {
    private lateinit var billsListLayout: LinearLayout
    private lateinit var totalTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(40, 40, 40, 40)
        }
        
        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 30)
        }
        
        val backButton = Button(this).apply {
            id = R.id.backButton
            text = "Back"
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 10, 20, 10)
        }
        
        val titleLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
        }
        
        titleLayout.addView(TextView(this).apply {
            text = "Past Due Bills"
            textSize = 28f
            gravity = Gravity.CENTER
        })
        
        totalTextView = TextView(this).apply {
            text = "Total: $0.00"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 0)
            setTextColor(android.graphics.Color.RED)
        }
        titleLayout.addView(totalTextView)
        
        headerLayout.addView(backButton)
        headerLayout.addView(titleLayout)
        mainLayout.addView(headerLayout)
        
        // Scrollable list
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        billsListLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        scrollView.addView(billsListLayout)
        mainLayout.addView(scrollView)
        
        setContentView(mainLayout)
        loadOverdueBills()
    }
    
    private fun loadOverdueBills() {
        GlobalScope.launch {
            val today = DateUtils.getCurrentIsoDate()
            val bills = database.billDao().getOverdueBills(today)
            
            runOnUiThread {
                billsListLayout.removeAllViews()
                
                if (bills.isEmpty()) {
                    billsListLayout.addView(TextView(this@PastDueBillsActivity).apply {
                        text = "No past due bills"
                        textSize = 18f
                        gravity = Gravity.CENTER
                        setPadding(0, 60, 0, 0)
                    })
                    totalTextView.text = "Total: $0.00"
                } else {
                    val total = bills.sumOf { it.amount + it.pastDueAmount }
                    totalTextView.text = "Total: $${String.format("%.2f", total)}"
                    
                    bills.forEach { bill ->
                        billsListLayout.addView(createBillItem(bill))
                    }
                }
            }
        }
    }
    
    private fun createBillItem(bill: BillEntity): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(30, 20, 30, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"))
            val params = layoutParams as LinearLayout.LayoutParams
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
            
            // Name and amount row
            val topRow = LinearLayout(this@PastDueBillsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            
            topRow.addView(TextView(this@PastDueBillsActivity).apply {
                text = bill.name
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
            
            topRow.addView(TextView(this@PastDueBillsActivity).apply {
                text = "$${String.format("%.2f", bill.amount)}"
                textSize = 20f
                setTextColor(android.graphics.Color.RED)
            })
            
            addView(topRow)
            
            // Due date and days overdue
            val dueDate = DateUtils.parseIsoDate(bill.dueDate)
            val today = Date()
            val daysOverdue = ((today.time - (dueDate?.time ?: 0)) / (1000 * 60 * 60 * 24)).toInt()
            
            addView(TextView(this@PastDueBillsActivity).apply {
                text = "Due: ${bill.dueDate} (${daysOverdue} days overdue)"
                textSize = 14f
                setPadding(0, 8, 0, 0)
                setTextColor(android.graphics.Color.parseColor("#C62828"))
            })
            
            // Show allocation info if any
            GlobalScope.launch {
                val totalAllocated = database.billPaymentAllocationDao().getTotalAllocatedForBill(bill.id, 1) ?: 0.0
                val totalPaid = database.billPaymentAllocationDao().getTotalPaidForBill(bill.id, 1) ?: 0.0
                
                if (totalAllocated > 0.0 || totalPaid > 0.0) {
                    runOnUiThread {
                        addView(TextView(this@PastDueBillsActivity).apply {
                            text = "💰 Allocated: $${String.format("%.2f", totalAllocated)} | Paid: $${String.format("%.2f", totalPaid)}"
                            textSize = 12f
                            setPadding(0, 4, 0, 0)
                            setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                        })
                        
                        val remaining = bill.amount - totalPaid
                        if (remaining > 0) {
                            addView(TextView(this@PastDueBillsActivity).apply {
                                text = "Remaining: $${String.format("%.2f", remaining)}"
                                textSize = 12f
                                setPadding(0, 2, 0, 0)
                                setTextColor(android.graphics.Color.parseColor("#E65100"))
                            })
                        }
                    }
                }
            }
            
            // Button row
            val buttonRow = LinearLayout(this@PastDueBillsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val buttonParams = layoutParams as LinearLayout.LayoutParams
                buttonParams.setMargins(0, 12, 0, 0)
                layoutParams = buttonParams
            }
            
            // Allocate button
            val allocateButton = Button(this@PastDueBillsActivity).apply {
                text = "Allocate $"
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPadding(20, 10, 20, 10)
                val allocateParams = layoutParams as LinearLayout.LayoutParams
                allocateParams.setMargins(0, 0, 8, 0)
                layoutParams = allocateParams
                
                setOnClickListener {
                    showAllocationDialog(bill)
                }
            }
            
            buttonRow.addView(allocateButton)
            
            // Mark paid button
            val payButton = Button(this@PastDueBillsActivity).apply {
                text = "Mark Paid"
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPadding(20, 10, 20, 10)
                
                setOnClickListener {
                    GlobalScope.launch {
                        database.billDao().updateBill(bill.copy(isPaid = true))
                        loadOverdueBills()
                    }
                }
            }
            
            buttonRow.addView(payButton)
            addView(buttonRow)
        }
    }
    
    private fun showAllocationDialog(bill: BillEntity) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        
        dialogLayout.addView(TextView(this).apply {
            text = "Allocate Payment for ${bill.name}"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        })
        
        dialogLayout.addView(TextView(this).apply {
            text = "Total due: $${String.format("%.2f", bill.amount)}"
            textSize = 14f
            setPadding(0, 0, 0, 10)
        })
        
        val amountInput = android.widget.EditText(this).apply {
            hint = "Amount to allocate"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(20, 20, 20, 20)
        }
        
        dialogLayout.addView(amountInput)
        
        dialogLayout.addView(TextView(this).apply {
            text = "From which paycheck?"
            textSize = 14f
            setPadding(0, 20, 0, 10)
        })
        
        val paycheckDateInput = android.widget.EditText(this).apply {
            hint = "yyyy-MM-dd (optional)"
            setPadding(20, 20, 20, 20)
        }
        
        dialogLayout.addView(paycheckDateInput)
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Allocate") { _, _ ->
                val amountText = amountInput.text.toString()
                if (amountText.isNotEmpty()) {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        GlobalScope.launch {
                            val allocation = BillPaymentAllocationEntity(
                                billId = bill.id,
                                userId = 1,
                                allocatedAmount = amount,
                                allocationDate = DateUtils.getCurrentIsoDate(),
                                paycheckDate = paycheckDateInput.text.toString().takeIf { it.isNotEmpty() },
                                notes = "Payment plan allocation"
                            )
                            database.billPaymentAllocationDao().insertAllocation(allocation)
                            runOnUiThread {
                                Toast.makeText(this@PastDueBillsActivity, "Allocated $${String.format("%.2f", amount)}", Toast.LENGTH_SHORT).show()
                                loadOverdueBills()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
}
