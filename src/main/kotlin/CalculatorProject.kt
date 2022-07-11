import java.math.BigInteger

class CalculatorProject {
    var stack = mutableListOf<String>()
    var queue = mutableListOf<String>()
    fun expParser(input: String): List<String> {
        val spacesRaw = input.replace(" ","")
        val spaces = spacesRaw.replace("\\d+|[a-zA-Z]".toRegex(), " ").replace("\\s+".toRegex()," ")

        val exp = mutableListOf<String>()
        for (ch in spaces) {
            exp.add(ch.toString())
        }

        val operands = input.replace("[()]".toRegex(),"").replace(" ","").split("[-+/*]".toRegex())

        var counter = 0
        val parsed = mutableListOf<String>()
        for (elem in exp) {
            if (elem == " ") {
                parsed.add(operands[counter])
                counter++
            } else {
                parsed.add(elem)
            }
        }

        return parsed
    }
    fun addToQueue(elem: String) {
        queue.add(elem)
    }
    fun push(elem: String) {
        stack.add(elem)
    }
    fun pop() {
        loop@for (i in stack.lastIndex downTo 0) {
            if (stack[i] == "(") {
                stack[i] = " "
                break@loop
            }
            addToQueue(stack[i])
            stack[i] = " "
        }
        stack.removeIf{it == " "}
    }
    fun infix(input: String) {
        val expression = expParser(input)
        for (elem in expression) {
            when {
                elem == "(" -> push(elem)
                elem == ")" -> {if (expression.contains("(")) pop() }
                "\\d".toRegex().containsMatchIn(elem) -> addToQueue(elem)
                "[a-zA-Z]".toRegex().containsMatchIn(elem) -> addToQueue(elem)
                "[+-]".toRegex().containsMatchIn(elem) -> {
                    if (stack.isEmpty() || stack.last() == "(") push(elem)
                    else if (stack.last().contains("[*/]".toRegex())) {
                        pop()
                        push(elem)
                    } else {
                        addToQueue(stack.last())
                        stack[stack.lastIndex] = elem
                    }
                }
                "[*/]".toRegex().containsMatchIn(elem) -> {
                    if (stack.isNotEmpty()   && (stack.last() == "*" || stack.last() == "/")) { pop() }
                    push(elem)
                }
            }
        }
        if (stack.isNotEmpty()) {
            for (i in stack.lastIndex downTo 0) {
                if (stack[i] != "(") { addToQueue(stack[i]) }
            }
        }
    }
    fun postfixCalc(store: MutableMap<String, BigInteger>) {
        val stack = mutableListOf<BigInteger>()
        loop2@for (elem in queue) {
            when {
                "\\d".toRegex().containsMatchIn(elem) -> stack.add(elem.toBigInteger())
                "[a-zA-Z]".toRegex().containsMatchIn(elem) -> {
                    if (store.containsKey(elem)) {
                        store[elem]?.let { stack.add(it) }
                    } else {

                        println("There is unassigned variable $elem in the expression!")
                        break@loop2
                    }
                }
                elem == "+" -> {
                    stack[stack.lastIndex - 1] += stack.last()
                    stack.removeLast()
                }
                elem == "/" -> {
                    stack[stack.lastIndex - 1] /= stack.last()
                    stack.removeLast()
                }
                elem == "*" -> {
                    stack[stack.lastIndex - 1] *= stack.last()
                    stack.removeLast()
                }
                elem == "-" -> {
                    stack[stack.lastIndex - 1] -= stack.last()
                    stack.removeLast()
                }
            }
        }
        println(stack.first())
    }

    fun main() {
        val store  = mutableMapOf<String, BigInteger>()
        loop@while (true) {
            var input = readln()
            input = input.replace(Regex("(--)+"),"+")
            input = input.replace(Regex("\\+-"),"-")
            input = input.replace("\\++ ".toRegex(),"+")
            input = input.replace("\\-+ ".toRegex(),"-")

            if (input.contains("/exit")) break
            if (input.contains("/help")) {
                println("The program performs mathematical evaluation")
                continue
            }
            if (input.isEmpty()) continue
            if (input.contains("^/".toRegex())) {
                println("Unknown command")
                continue
            }
            var par1 = 0
            var par2 = 0
            for (ch in input) {
                if (ch == ')') par1++ else if (ch == '(') par2++
            }
            if (par1 != par2 || (input.contains("**") || input.contains("//"))) {
                println("Invalid expression")
                continue
            }

            if (input.matches("\\s*[a-zA-Z]+\\s*=\\s*-?\\d+\\s*".toRegex())) {
                input = input.replace(" ", "")
                val (key, value ) = input.split("=")
                store[key] = value.toBigInteger()
                continue
            }
            if (input.matches("\\s*[a-zA-Z]+.*=\\s*\\D*\\d+[a-zA-z]+=?\\s*\\d*".toRegex())) {
                println("Invalid assignment")
                continue
            }
            if (input.matches("[a-zA-Z]+\\s*=\\s*[a-zA-z]+\\s*".toRegex())) {
                input = input.replace(" ", "")
                val (key, value ) = input.split("=")
                if (store.containsKey(value)) store[value]?.let { store.put(key, it) } else println("Unknown variable")
                continue
            }
            if (input.matches(".*[a-zA-Z]+.*\\s*=\\s*\\d+".toRegex()) || input.contains("[^\\u0000-\\u024F]+".toRegex())) {
                println("Invalid identifier")
                continue
            }
            if (input.matches("[a-zA-Z]*".toRegex()) || input.matches("[a-zA-Z]+\\s*=\\s*[a-zA-Z]+".toRegex())) {
                println(if (store.containsKey(input)) store[input] else "Unknown variable")
                continue
            }
            if (!input.contains('=')) {
                expParser(input)
                infix(input)
                postfixCalc(store)
                queue.clear()
                stack.clear()
            }
        }
        println("Bye!")
    }
}