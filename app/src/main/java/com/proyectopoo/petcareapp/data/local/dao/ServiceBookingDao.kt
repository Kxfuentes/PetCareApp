package com.proyectopoo.petcareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.proyectopoo.petcareapp.data.local.entity.BookingStatus
import com.proyectopoo.petcareapp.data.local.entity.ServiceBookingEntity

@Dao
interface ServiceBookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(
        booking: ServiceBookingEntity
    )

    @Query("SELECT * FROM service_bookings")
    suspend fun getAllBookings(): List<ServiceBookingEntity>

    @Query("""
        SELECT * FROM service_bookings
        WHERE bookingId = :bookingId
        LIMIT 1
    """)
    suspend fun getBookingById(
        bookingId: Int
    ): ServiceBookingEntity?

    @Query("""
        SELECT * FROM service_bookings
        WHERE caregiverId = :caregiverId
    """)
    suspend fun getBookingsByCaregiver(
        caregiverId: Int
    ): List<ServiceBookingEntity>

    @Query("""
        SELECT * FROM service_bookings
        WHERE status = :status
    """)
    suspend fun getBookingsByStatus(
        status: BookingStatus
    ): List<ServiceBookingEntity>

    @Query("""
        UPDATE service_bookings
        SET status = :status
        WHERE serviceRequestId = :serviceRequestId
    """)
    suspend fun updateStatusByRequest(
        serviceRequestId: Int,
        status: BookingStatus
    )

    @Update
    suspend fun updateBooking(
        booking: ServiceBookingEntity
    )

    @Delete
    suspend fun deleteBooking(
        booking: ServiceBookingEntity
    )

    @Query("""
        SELECT * FROM service_bookings
        WHERE serviceRequestId IN (
            SELECT serviceRequestId FROM service_requests WHERE ownerId = :ownerId
        )
        ORDER BY bookingId DESC
    """)
    suspend fun getBookingsByOwner(ownerId: Int): List<ServiceBookingEntity>
}