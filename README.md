# Netclocks (aka clocks of unknown origin)

A few years ago, the Clocksmith on our campus talked to me about an idea.  We were spending close to $1,000 per clock for a wirelessly networked clock unit from our security company.  He wondered if we could do something better.  He found essentially the same base unit from Ali Express.  Its just a cheap LED sign that you can see in shops all over the world.  I think it cost $30.  It came with a terrible program to change the message.

I reverse engineered the comms protocol, added a [TI CC3200](https://www.ti.com/product/CC3200), and used it to translate from WiFi to the on-board controller's serial port (which is how it originally connected to a PC).

Then we modified the CC3200 to keep time and send the time to the display every second.

Finally, I created this Java program to allow us to send alert messages - like "Shelter in Place," something that was supposed to work in the $1,000 units, but didn't.  

The University opted to spend > $150,000 to buy the security clocks, instead of $4,500 and an undergrad to solder six connections to add the new circuit board.  Go figure.
