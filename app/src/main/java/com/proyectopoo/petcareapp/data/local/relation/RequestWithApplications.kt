package com.proyectopoo.petcareapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.proyectopoo.petcareapp.data.local.entity.ServiceApplicationEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestEntity

data class RequestWithApplications(

    @Embedded
    val request: ServiceRequestEntity,

    @Relation(
        parentColumn = "serviceRequestId",
        entityColumn = "serviceRequestId"
    )
    val applications: List<ServiceApplicationEntity>
)