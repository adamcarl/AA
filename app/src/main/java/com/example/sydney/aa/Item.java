package com.example.sydney.aa;

/**
 * Created by PROGRAMMER2 on 5/2/2017.
 */
class Item {
    private String status;
    private String barcode;
    private String description;
//    private int quantity;

    Item(String barcode, String description, String status) {
        this.status = status;
        this.barcode = barcode;
        this.description = description;
//        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public int getQuantity() {
//        return quantity;
//    }

//    public void setQuantity(int quantity) {
//        this.quantity = quantity;
//    }

    Item(){

    }
}