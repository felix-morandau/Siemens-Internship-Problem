package com.siemens.internship.config;

import com.siemens.internship.model.Item;

public class ItemNotFoundException extends RuntimeException{

    public  ItemNotFoundException() {
        super("Item was not found.");
    }

    public ItemNotFoundException(Long id) {
        super("Item with id " + id + " was not found");
    }
}
