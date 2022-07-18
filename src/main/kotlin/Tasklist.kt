package tasklist


import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import okio.buffer
import okio.source
import java.io.File

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlin.text.StringBuilder
data class Task(var id: String, var date: String, var time: String, var priority: String, var overdue: String, var task: String)
const val DELIMITER = "+----+------------+-------+---+---+--------------------------------------------+\n"
val jsonFile = File("tasklist.json")

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

@OptIn(ExperimentalStdlibApi::class)
fun saveFile(taskList: MutableList<Task>) {
    val taskAdapter = moshi.adapter<List<Task>>()
    val toFile = taskAdapter.indent(" ").toJson(taskList)
    jsonFile.writeText(toFile)
}
@OptIn(ExperimentalStdlibApi::class)
fun loadFile(taskList: MutableList<Task>) {
    val taskAdapter = moshi.adapter<List<Task>>()
    val parsedJson = taskAdapter.fromJson(jsonFile.inputStream().source().buffer())
    if (parsedJson != null) {
        for (task in parsedJson) {
            try {
                val tsk = Task((taskList.size+1).toString(),task.date, task.time, task.priority, task.overdue, task.task)
                taskList.add(tsk)
            } catch (e: Exception) {
                println(e.message)
            }
        }

    }
}

fun header(): StringBuilder {
    val header = StringBuilder()
    val names = "| N  |    Date    | Time  | P | D |                   Task                     |\n"
    header.append(DELIMITER, names, DELIMITER)
    return header
}
fun printing(taskList: MutableList<Task>) {
    var rawTasks = mutableListOf<String>()
    val builder = header()
    for (task in taskList) {
        if (task.task.contains(";")) {
            rawTasks = task.task.split(";").toMutableList()
        }
        if (rawTasks.isNotEmpty()){

            val subTasks = mutableListOf<String>()
            var tasksToPrint = mutableListOf<String>()
            for (index in rawTasks.indices) {
                if (rawTasks[index] != "") subTasks.add(rawTasks[index])

                for (subtask in subTasks) {
                    if (subtask.length > 44) {
                        tasksToPrint.addAll(subtask.chunked(44))
                    } else {
                        tasksToPrint.add(subtask)
                    }
                }
                tasksToPrint = tasksToPrint.toSet().toMutableList()
            }


            if (tasksToPrint.size == 1) {
                if (task.id.toInt() < 10) {
                    builder.append("| ${task.id}  | ${task.date} | ${task.time} | ${task.priority} | ${task.overdue} |${tasksToPrint.first().padEnd(44)}|")
                } else {
                    builder.append("| ${task.id} | ${task.date} | ${task.time} | ${task.priority} | ${task.overdue} |${tasksToPrint.first().padEnd(44)}|")
                }
            } else {
                if (task.id.toInt() < 10) {
                    builder.append("| ${task.id}  | ${task.date} | ${task.time} | ${task.priority} | ${task.overdue} |${tasksToPrint.first().padEnd(44)}|\n")
                } else {
                    builder.append("| ${task.id}  | ${task.date} | ${task.time} | ${task.priority} | ${task.overdue} |${tasksToPrint.first().padEnd(44)}|\n")
                }
                for (index in 1 until tasksToPrint.lastIndex) {
                    builder.append("|    |            |       |   |   |${tasksToPrint[index].padEnd(44)}|\n")
                }
                builder.append("|    |            |       |   |   |${tasksToPrint.last().padEnd(44)}|")
            }

        }
        builder.append("\n"+"+----+------------+-------+---+---+--------------------------------------------+\n")
    }
    println(builder)
}
fun deleting(taskList: MutableList<Task>) {

    println("Input the task number (${taskList.first().id}-${taskList.last().id.toInt()}):")
    val taskNumber = readln().toInt()
    taskList.removeAt(taskList.indexOf(taskList.find { task: Task -> task.id.toInt() == taskNumber }))
    for (index in taskList.indices) {
        taskList[index].id = (taskList[index].id.toInt()-1).toString()
    }
    println("The task is deleted")
}
fun dateValidator() : String {
    var date: String
    do {
        println("Input the date (yyyy-mm-dd):")
        date = readln().trim()
        val dateNumbers = date.split("-").toMutableList()
        for (index in dateNumbers.indices) {
            if (dateNumbers[index].length == 1) dateNumbers[index] = "0" + dateNumbers[index]
        }
        date = dateNumbers.joinToString("-")
        try {
            date = LocalDate.parse(date).toString()
        } catch (e: DateTimeParseException) {
            date = ""
            println("The input date is invalid")
            continue
        }
    } while (date == "")
    return date

}
fun overdueCheck (date: String) : String {
    val taskDate =kotlinx.datetime.LocalDate.parse(date)
    val currentDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.of("UTC+2")).date
    val timer = currentDate.daysUntil(taskDate)
    val tag = when {
        timer == 0 -> "\u001b[103m \u001B[0m"
        timer > 0 -> "\u001B[102m \u001B[0m"
        else -> "\u001B[101m \u001B[0m"
    }
    return tag
}
fun timeValidator() : String {
    var time: String
    do {
        println("Input the time (hh:mm):")
        time = readln().trim()
        val timeNumbers = time.split(":").toMutableList()
        for (index in timeNumbers.indices) {
            if (timeNumbers[index].length == 1) timeNumbers[index] = "0" + timeNumbers[index]
        }
        time = timeNumbers.joinToString(":")
        try {
            time = LocalTime.parse(time).toString()
        } catch (e: DateTimeParseException) {
            time = ""
            println("The input time is invalid")
            continue
        }
    } while (time == "")
    return time
}
fun priority(): String {
    var taskPriority: String
    val priorities = listOf("C","H","N","L")
    do {
        println("Input the task priority (C, H, N, L):")
        taskPriority = readln().trim().uppercase()
    } while (taskPriority !in priorities)
    return when(taskPriority) {
        "C" -> "\u001B[101m \u001B[0m"
        "H" -> "\u001B[103m \u001B[0m"
        "N" -> "\u001B[102m \u001B[0m"
        "L" -> "\u001B[104m \u001B[0m"
        else -> taskPriority
    }
}
fun addTask() : String {
    var task = ""
    do {
        val line = readln()
        task += line.trim()+";"
    } while (!line.matches("\\s*".toRegex()) )
    return task
}


fun main() {
    val taskList = mutableListOf<Task>()
    if (jsonFile.exists()) loadFile(taskList)
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        when (readln().trim()) {
            "add" -> {

                val taskPriority = priority()
                val date= dateValidator()
                val time = timeValidator()
                val overdue = overdueCheck(date)
                println("Input a new task (enter a blank line to end):")
                val taskBody = addTask()
                if (taskBody.contains("[a-zA-Z]+|\\d+".toRegex())) {
                    val task = Task("${taskList.size + 1}", date, time, taskPriority, overdue, taskBody )
                    taskList.add(task)
                } else {
                    println("The task is blank")
                }
                continue
            }
            "print" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                } else {
                    printing(taskList)
                }
                continue
            }
            "edit" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                } else {
                    printing(taskList)

                    var taskNumber: Int
                    while (true) {
                        try {
                            println("Input the task number (1-${taskList.lastIndex+1}):")
                            taskNumber = readln().toInt()
                            if (taskNumber in 1..taskList.lastIndex+1){
                                break
                            } else {
                                println("Invalid task number")
                                continue
                            }
                        } catch (e: Exception) {
                            println("Invalid task number")
                        }
                    }
                    while (true) {
                        println("Input a field to edit (priority, date, time, task):")
                        when (readln()) {
                            "priority" -> {
                                taskList[taskNumber-1].priority = priority()
                                break
                            }
                            "date" -> {
                                taskList[taskNumber-1].date = dateValidator()
                                break
                            }
                            "time" -> {
                                taskList[taskNumber-1].time = timeValidator()
                                break
                            }
                            "task" -> {
                                println("Input a new task (enter a blank line to end):")
                                taskList[taskNumber-1].task = addTask()
                                break
                            }
                            else -> {
                                println("Invalid field")
                                continue
                            }
                        }
                    }


                    println("The task is changed")
                }
                continue
            }
            "delete" -> {
                if (taskList.isEmpty()) {
                    println("No tasks have been input")
                } else {
                    printing(taskList)
                    while (true){
                        try {
                            deleting(taskList)
                            break
                        } catch (e: Exception) {
                            println("Invalid task number")
                            continue
                        }
                    }
                }
                continue
            }
            "end" -> {
                if (taskList.isNotEmpty()) saveFile(taskList)
                println("Tasklist exiting!")
                break
            }
            else -> {
                println("The input action is invalid")
                continue
            }
        }


    }}
