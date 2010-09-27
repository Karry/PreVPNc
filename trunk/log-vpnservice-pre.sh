#!/bin/sh

mkdir /media/internal/.vpn

echo "address=127.0.0.1" > /media/internal/.vpn/debug.conf
echo "port=1234" >> /media/internal/.vpn/debug.conf
echo "debug=true" >> /media/internal/.vpn/debug.conf

killall -9 java

while [ 1 ] ; do
	netcat -lv -p 1234
done > /media/internal/vpnservice.log
