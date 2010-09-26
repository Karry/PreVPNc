#!/bin/sh

umount -f /opt/vpnbox/etc
umount -f /opt/vpnbox/proc
cd /opt/
tar -cf - vpnbox | gzip > /media/internal/vpnbox.tar.gz
