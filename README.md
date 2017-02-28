# BeaconRooms
Testing Beacon detection & Uses Twilio Video to create Rooms for each Beacon

Used Nodejs to create eddystone beacons on mac. https://github.com/don/node-eddystone-beacon

Physical Web App on Android for other beacons. 

Tested for Url, Uid, Power. Currently only displays in Console. 

Modified format for beacon(Just for testing):

    var eddystoneBeacon = require('./../../index');

    eddystoneBeacon.advertiseUrl('http://room=TosiasRoom', { name: 'TosiasRoom' });

The MainActivity detects a room and uses the 'room=' to extract the roomname.

It then allows users to join the room.

Currently limited to 2 participants per room.(Same as Twilio Example)
