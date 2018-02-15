import hudson.plugins.ircbot.v2.IRCConnectionProvider;
import hudson.plugins.im.*

def call(message, room = "#aerogear") {
    try {
    def conP = IRCConnectionProvider.getInstance()
    def con = conP.currentConnection()
    con.send(room, message)
    } catch (err) {
        print "There was a problem when trying to send message to ${room} through IRC Plugin"
        print err
    } 
}
