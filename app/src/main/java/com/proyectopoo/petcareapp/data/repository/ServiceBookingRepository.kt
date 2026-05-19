package com.proyectopoo.petcareapp.data.repository

import com.proyectopoo.petcareapp.data.local.dao.ServiceBookingDao
import com.proyectopoo.petcareapp.data.local.entity.BookingStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity

class ServiceBookingRepository(
    private val bookingDao: ServiceBookingDao
) {

    suspend fun insertBooking(
        booking: ServiceBookingEntity
    ) {
        bookingDao.insertBooking(booking)
    }

    suspend fun getAllBookings(): List<ServiceBookingEntity> {
        return bookingDao.getAllBookings()
    }

    suspend fun getBookingById(
        bookingId: Int
    ): ServiceBookingEntity? {

        return bookingDao.getBookingById(bookingId)
    }

    suspend fun getBookingsByCaregiver(
        caregiverId: Int
    ): List<ServiceBookingEntity> {

        return bookingDao.getBookingsByCaregiver(caregiverId)
    }

    suspend fun getBookingsByStatus(
        status: BookingStatus
    ): List<ServiceBookingEntity> {

        return bookingDao.getBookingsByStatus(status)
    }

    suspend fun updateBooking(
        booking: ServiceBookingEntity
    ) {
        bookingDao.updateBooking(booking)
    }

    suspend fun deleteBooking(
        booking: ServiceBookingEntity
    ) {
        bookingDao.deleteBooking(booking)
    }
}