package com.billybobbain.fractald.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()

    suspend fun getBookmarkById(id: Long): Bookmark? {
        return bookmarkDao.getBookmarkById(id)
    }

    suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun update(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    suspend fun delete(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    suspend fun deleteById(id: Long) {
        bookmarkDao.deleteBookmarkById(id)
    }
}
