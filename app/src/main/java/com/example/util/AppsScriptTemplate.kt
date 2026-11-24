package com.example.util

object AppsScriptTemplate {
    val CODE: String = """
function doPost(e) {
  try {
    var ss = SpreadsheetApp.getActiveSpreadsheet();
    var payload = JSON.parse(e.postData.contents);

    // 1. Sheet Transaksi
    var txSheet = getOrCreateSheet(ss, "Transaksi", [
      "No_Invoice", "Tanggal", "Pelanggan", "Metode", "Total_Tagihan", "Dibayar", "Kembalian_Sisa", "Rincian_Barang"
    ]);
    if (payload.transactions && payload.transactions.length > 0) {
      payload.transactions.forEach(function(tx) {
        txSheet.appendRow([
          tx.invoiceNumber, tx.dateFormatted, tx.customerName,
          tx.paymentMethod, tx.totalAmount, tx.paidAmount,
          tx.changeOrRemaining, tx.itemsSummary
        ]);
      });
    }

    // 2. Sheet Stok_Produk (Update penuh stok terbaru)
    var stokSheet = getOrCreateSheet(ss, "Stok_Produk", [
      "Barcode", "Nama_Produk", "Kategori", "Harga_Modal", "Harga_Jual", "Stok_Saat_Ini", "Batas_Min"
    ]);
    if (payload.products && payload.products.length > 0) {
      if (stokSheet.getLastRow() > 1) {
        stokSheet.getRange(2, 1, stokSheet.getLastRow() - 1, 7).clearContent();
      }
      payload.products.forEach(function(p) {
        stokSheet.appendRow([
          p.barcode, p.name, p.category, p.costPrice, p.sellPrice, p.stock, p.minStock
        ]);
      });
    }

    // 3. Sheet Buku_Kasbon
    var kasbonSheet = getOrCreateSheet(ss, "Buku_Kasbon", [
      "Tanggal", "Nama_Pelanggan", "No_WhatsApp", "Jenis", "Nominal", "Catatan", "Sisa_Utang"
    ]);
    if (payload.kasbon && payload.kasbon.length > 0) {
      payload.kasbon.forEach(function(k) {
        kasbonSheet.appendRow([
          k.dateFormatted, k.customerName, k.whatsapp, k.type, k.amount, k.note, k.remainingDebt
        ]);
      });
    }

    return ContentService
      .createTextOutput(JSON.stringify({ status: "success", syncedAt: new Date().toISOString() }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService
      .createTextOutput(JSON.stringify({ status: "error", message: err.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function getOrCreateSheet(ss, name, headers) {
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    sheet = ss.insertSheet(name);
    sheet.appendRow(headers);
    sheet.getRange(1, 1, 1, headers.length).setFontWeight("bold");
  }
  return sheet;
}
""".trimIndent()
}
