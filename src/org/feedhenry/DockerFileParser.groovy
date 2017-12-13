import groovy.json.JsonSlurper

class Command {
    Object args
    CMD name
    String error
    int lineno
    String raw
    String rest

    String toString() {
        "Command[lineno: $lineno, name: $name, rest: $rest, error: $error, args: $args]"
    }
}

enum TOKEN {
    COMMENT(/^#.*$/),
    WHITESPACE(/[\t\v\f\r\s]+/),
    WORDS_GROUPS(/([^\s"']*"[^"]*"|[^\s"']*'[^']*'|[^\s"']+)+/),
    LINE_CONTINUATION(/\\[\s\t]*$/),
    NEW_LINE(/[\r?\n]/)

    final String token

    TOKEN(token) {
        this.token = token
    }
}

enum CMD {
    EMPTY('emptyParserFn'),
    ADD('parseJsonOrList'),
    ARG('parseNameOrNameVal'),
    CMD('parseJsonOrString'),
    COMMENT('parseString'),
    COPY('parseJsonOrList'),
    ENTRYPOINT('parseJsonOrString'),
    ENV('parseEnv'),
    EXPOSE('parseStringsWhitespaceDelimited'),
    FROM('parseString'),
    LABEL('parseLabel'),
    MAINTAINER('parseString'),
    ONBUILD('parseSubCommand'),
    RUN('parseJsonOrString'),
    STOPSIGNAL('parseString'),
    USER('parseString'),
    VOLUME('parseJsonOrList'),
    WORKDIR('parseString')

    final operation

    CMD(operation) {
        this.operation = operation
    }
}

def parseNameVal(cmd) {
    def words = parseWords(cmd.rest)
    def result
    cmd.args = [:]

    if (words.isEmpty()) {
        cmd.error = 'No KEY name value, or KEY name=value arguments found'
        result = false
    }

    if (words[0] =~ '=') { // new format: KEY name1=value1 name2=value2 ...
        words.each { w ->
            if (w =~ '=') { // process each nameX=valueX pair
                def parts = w.split('=')
                cmd.args[parts[0]] = parts[1]
            } else {
                cmd.error = "Syntax error - can't find = in ${w}. Must be of the form: name=value"
                result = false
            }
        }
    } else { // old format: KEY name value
        def strs = cmd.rest.split(TOKEN.WHITESPACE.token)
        if (strs.length == 2) {
            cmd.args[strs[0]] = strs[1]
        } else {
            cmd.error = cmd.name + ' must have two arguments, got: ' + cmd.rest
            result = false
        }
    }
    result
}

def parseWords(rest) {
    def matchList = []
    def matcher = (rest =~ TOKEN.WORDS_GROUPS.token)
    while (matcher.find()) {
        matchList.add(matcher.group())
    }
    matchList
}

boolean isListOrArray(obj) {
    [List, Object[]].any { it.isAssignableFrom(obj.getClass()) }
}

boolean isString(obj) {
    [String, GString].any { it.isAssignableFrom(obj.getClass()) }
}

// Converts to JSON array, returns true on success, false otherwise.
def parseJSON(cmd) {
    def json
    def result
    try {
        json = new JsonSlurper().parseText(cmd.rest)
    } catch (e) {
        result = false
    }
    // Ensure it's an List or Array[]
    if (!isListOrArray(json)) {
        result = false
    }
    // Ensure every entry in the array of String
    def isAnyNotString = json.collect {
        isString(it)
    }.any { it == false }

    if (isAnyNotString) {
        result = false
    }

    cmd.args = json
    result
}

def isComment(line) {
    line =~ TOKEN.COMMENT.token
}

// Takes a single line of text and parses out the name and rest,
// which are used for dispatching to more exact parsing functions.
def splitCommand(line) {
    Command command = new Command()
    // Make sure we get the same results irrespective of leading/trailing spaces
    def match = line =~ TOKEN.WHITESPACE.token
    if (match) {
        def twsIdx = line.findIndexOf { it =~ TOKEN.WHITESPACE.token }
        def name = line[0..twsIdx - 1].toUpperCase()
        def rest = line[twsIdx + 1..-1]
        command.name = name as CMD
        command.rest = rest
    } else {
        command.name = line.toUpperCase() as CMD
        command.rest = null
    }

    command
}

//###############################################################################
def emptyParserFn = {}

def parseNameOrNameVal = { cmd ->
    cmd.args = parseWords(cmd.rest)
}

def parseEnv = { cmd ->
    parseNameVal(cmd)
}

def parseStringsWhitespaceDelimited = { cmd ->
    cmd.args = cmd.rest.split(TOKEN.WHITESPACE.token)
}

def parseJsonOrList = { cmd ->
    parseJSON(cmd) ? true : parseStringsWhitespaceDelimited(cmd)
}

def parseString = { cmd ->
    cmd.args = cmd.rest
}

def parseJsonOrString = { cmd ->
    parseJSON(cmd) ? true : parseString(cmd)
}

def parseLabel = { cmd ->
    parseNameVal(cmd)
}

def parseSubCommand = { cmd ->
    def parseDetails = parseLine(cmd.rest, cmd.lineno)
    def result
    if (parseDetails.command) {
        cmd.args = parseDetails.command
        result = true
    } else {
        cmd.error = 'Unhandled onbuild command: ' + cmd.rest
        result = false
    }
    result
}
//###############################################################################
def PARSER_FNS = [
        'emptyParserFn'                  : emptyParserFn,
        'parseNameOrNameVal'             : parseNameOrNameVal,
        'parseJsonOrList'                : parseJsonOrList,
        'parseJsonOrString'              : parseJsonOrString,
        'parseEnv'                       : parseEnv,
        'parseStringsWhitespaceDelimited': parseStringsWhitespaceDelimited,
        'parseString'                    : parseString,
        'parseLabel'                     : parseLabel,
        'parseSubCommand'                : parseSubCommand,
]

// parse a line and return the remainder.
def parseLine(line, lineno, parserFns = [:], options = [:]) {

    def result
    def lineContinuationRegex = options?.lineContinuationRegex ?: TOKEN.LINE_CONTINUATION.token
    line = line.trim()

    if (line.isEmpty()) {
        // Ignore empty lines
        Command command = new Command()
        command.name = CMD.EMPTY
        return [command: command, remainder: '']
    } else if (isComment(line)) {
        // Handle comment lines
        Command command = new Command()
        command.name = CMD.COMMENT
        command.raw = line
        command.rest = null
        command.lineno = lineno

        result = [command: command, remainder: '']
    } else if (line =~ lineContinuationRegex) {
        // Line continues on next line.
        def remainder = line.replaceAll(lineContinuationRegex, '')
        Command command = new Command()
        command.name = CMD.EMPTY
        result = [command: command, remainder: remainder]
    } else {
        Command command = splitCommand(line)
        command.lineno = lineno
        def commandParserFn = parserFns[command.name.operation]
        if (commandParserFn(command)) {
            // Successfully converted the arguments.
            command.raw = line
//            command.rest = null TODO dont change command.args
        }

        result = [command: command, remainder: '']
    }
    return result
}

def parse(String contents, parserFns, options = [:]) {
    def commands = []
    def lines = contents.split(TOKEN.NEW_LINE.token)
    def regexMatch
    def remainder = null
    def includeComments = options?.includeComments ?: false
    def includeEmpty = options?.includeEmpty ?: false

    lines.eachWithIndex { String ln, int lnNo ->
        def lineno = lnNo + 1
        def line = remainder ? remainder + ln : ln
        def parseResult = parseLine(line, lineno, parserFns, options)
        if (parseResult.command) {
            if ((parseResult.command.name != CMD.COMMENT || includeComments) && (parseResult.command.name != CMD.EMPTY || includeEmpty)) {
                commands << parseResult.command
            }
        }
        remainder = parseResult.remainder
    }

    return commands
}

def contents = '''
PUT DOCKER FILE CONTENT HERE
'''

def commands = parse(contents, PARSER_FNS)
commands.each { println(it) }
