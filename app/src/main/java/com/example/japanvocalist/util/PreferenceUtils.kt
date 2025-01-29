package com.example.japanvocalist.util

fun buildIndexKey(categoryId: Int, offset: Int): String {
    return "indexList_${categoryId}_${offset}"
}

fun buildKnownWordsKey(categoryId: Int, offset: Int): String {
    return "known_words_${categoryId}_${offset}"
}