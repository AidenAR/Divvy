package com.example.divvy.backend

import com.example.divvy.models.ParsedReceipt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannedReceiptStore @Inject constructor() {
    private var _receipt: ParsedReceipt? = null

    fun store(receipt: ParsedReceipt) {
        _receipt = receipt
    }

    fun peek(): ParsedReceipt? = _receipt

    fun consume(): ParsedReceipt? {
        val receipt = _receipt
        _receipt = null
        return receipt
    }

    fun clear() {
        _receipt = null
    }
}
