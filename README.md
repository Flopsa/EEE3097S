# EEE3097S
Electrical and computer engineering design project.

### Getting BT working
To get BT working between android app and transmitting RPi:
Run on RPi:
` sudo apt-get install bluez python-bluez`

Modify /etc/systemd/system/dbus-org.bluez.service:
`ExecStart=/usr/lib/bluetooth/bluetoothd -C`

Then run:
`sudo sdptool add SP`

Reboot.

### Finding BT settings
Make BT discoverable:
`sudo hciconfig hci0 piscan`

Find BT mac address:
`hciconfig`
