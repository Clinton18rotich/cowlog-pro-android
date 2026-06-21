package com.cowlog.pro.data

data class ProjectSettings(
    var projectName: String = "",
    var contractNo: String = "",
    var contractorName: String = "",
    var defaultLocation: String = "",
    var cowName: String = "Clerk of Works",
    var geminiKey: String = "",
    var reportCounter: Int = 1,
    var pin: String = ""
)

data class DiaryEntry(
    val id: String = "",
    val title: String = "",
    val activityType: String = "Work in Progress",
    val location: String = "",
    val description: String = "",
    val labourCount: String = "",
    val equipmentUsed: String = "",
    val materialsDelivered: String = "",
    val issuesIdentified: String = "",
    val percentComplete: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Inspection(
    val id: String = "",
    val checklistType: String = "",
    val location: String = "",
    val items: List<ChecklistItem> = emptyList(),
    val result: String = "pass",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val label: String = "",
    val passed: Boolean = true,
    val comment: String = ""
)

data class NCR(
    val id: String = "",
    val title: String = "",
    val severity: String = "Major",
    val location: String = "",
    val responsibleParty: String = "",
    val description: String = "",
    val actionRequired: String = "",
    val status: String = "open",
    val closeoutComment: String = "",
    val closedDate: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

data class SiteInstruction(
    val id: String = "",
    val issuedTo: String = "",
    val location: String = "",
    val description: String = "",
    val reference: String = "",
    val deadline: String = "",
    val status: String = "issued",
    val acknowledgedDate: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

data class RFI(
    val id: String = "",
    val subject: String = "",
    val sentTo: String = "",
    val question: String = "",
    val response: String = "",
    val responseDate: Long = 0L,
    val status: String = "open",
    val timestamp: Long = System.currentTimeMillis()
)

data class Drawing(
    val id: String = "",
    val number: String = "",
    val title: String = "",
    val discipline: String = "Structural",
    val revision: String = "A",
    val dateReceived: String = "",
    val imageData: String = "",
    val aiAnalysis: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Material(
    val id: String = "",
    val name: String = "",
    val unit: String = "Bags",
    val initialStock: Double = 0.0,
    var currentStock: Double = 0.0,
    val minStock: Double = 0.0
)

data class MaterialLog(
    val id: String = "",
    val materialId: String = "",
    val type: String = "usage",
    val quantity: Double = 0.0,
    val unit: String = "",
    val supplier: String = "",
    val deliveryNote: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ConcreteCube(
    val id: String = "",
    val cubeId: String = "",
    val pourDate: String = "",
    val location: String = "",
    val grade: String = "C30/37",
    val result7: Double? = null,
    val result14: Double? = null,
    val result28: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Delay(
    val id: String = "",
    val date: String = "",
    val cause: String = "",
    val duration: String = "1",
    val impact: String = "Major",
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Attendance(
    val id: String = "",
    val date: String = "",
    val category: String = "Skilled Labour",
    val name: String = "",
    val company: String = "",
    val count: String = "1",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppData(
    val diary: MutableList<DiaryEntry> = mutableListOf(),
    val inspections: MutableList<Inspection> = mutableListOf(),
    val ncrs: MutableList<NCR> = mutableListOf(),
    val instructions: MutableList<SiteInstruction> = mutableListOf(),
    val rfis: MutableList<RFI> = mutableListOf(),
    val drawings: MutableList<Drawing> = mutableListOf(),
    val materials: MutableList<Material> = mutableListOf(),
    val materialLogs: MutableList<MaterialLog> = mutableListOf(),
    val concrete: MutableList<ConcreteCube> = mutableListOf(),
    val delays: MutableList<Delay> = mutableListOf(),
    val attendance: MutableList<Attendance> = mutableListOf(),
    val meetings: MutableList<MeetingMinutes> = mutableListOf(),
    val stopWorkOrders: MutableList<StopWorkOrder> = mutableListOf(),
    val delayNotices: MutableList<DelayNotice> = mutableListOf(),
    val materialRejections: MutableList<MaterialRejection> = mutableListOf(),
    val plantEquipment: MutableList<PlantEquipment> = mutableListOf(),
    val plantDailyLogs: MutableList<PlantDailyLog> = mutableListOf()
)

// NEW DOCUMENT MODELS
data class MeetingMinutes(
    val id: String = "",
    val meetingNo: String = "",
    val date: String = "",
    val time: String = "10:00 AM",
    val venue: String = "",
    val attendees: String = "",
    val previousMinutes: String = "",
    val progressUpdate: String = "",
    val qualityReview: String = "",
    val actionItems: String = "",
    val nextMeetingDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StopWorkOrder(
    val id: String = "",
    val orderNo: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val reason: String = "",
    val workToStop: String = "All work at specified location",
    val resumptionConditions: String = "",
    val status: String = "active",
    val resumedDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class DelayNotice(
    val id: String = "",
    val noticeNo: String = "",
    val date: String = "",
    val delayCommenced: String = "",
    val durationDays: String = "1",
    val estimatedTotalDays: String = "",
    val activityAffected: String = "",
    val location: String = "",
    val cause: String = "",
    val causeCategory: String = "Contractor's fault",
    val impactAssessment: String = "",
    val criticalPathAffected: String = "No",
    val eotRequired: String = "No",
    val mitigation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class MaterialRejection(
    val id: String = "",
    val mrnNo: String = "",
    val date: String = "",
    val supplier: String = "",
    val material: String = "",
    val quantityRejected: String = "",
    val deliveryNote: String = "",
    val reasonCategory: String = "Poor quality",
    val detailedReason: String = "",
    val actionRequired: String = "Remove from site and replace",
    val replacementDeadline: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// PLANT & EQUIPMENT
data class PlantEquipment(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val owner: String = "Contractor",
    val dateOnSite: String = "",
    val dateOffSite: String = "",
    val status: String = "Working", // Working, Idle, Under Repair, Demobilized
    val hoursWorkedToday: String = "0",
    val idleReason: String = "",
    val fuelUsed: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class PlantDailyLog(
    val id: String = "",
    val plantId: String = "",
    val date: String = "",
    val hoursWorked: String = "0",
    val hoursIdle: String = "0",
    val idleReason: String = "",
    val fuelIssued: String = "",
    val operatorName: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
