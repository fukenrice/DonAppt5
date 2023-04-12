package com.example.donappt5.data.model

class SearchContext(var tags: HashMap<String, Boolean>, var name: String) {
    fun isEmpty(): Boolean {
        return tags["kids"] != true && tags["poverty"] != true && tags["healthcare"] != true &&
                tags["science"] != true && tags["art"] != true && tags["education"] != true
                && name.isEmpty()
    }
}