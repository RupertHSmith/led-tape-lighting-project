#!/usr/bin/python

import firebase_admin
import google as google
import time
import socket
import json
from firebase_admin import credentials, firestore
from threading import Thread

import logging

class TcpListener(Thread):
    def __init__(self, socket, db):
        Thread.__init__(self)
        self.socket = socket
        self.db = db
        self.daemon = True
        self.start()
    def run(self):
        while True:
            try:
                dat = self.socket.recv(6)
                start_pipe = dat[:1]
                standby = (dat[1:2] == '1')
                intensity = int(dat[2:5])
                end_pipe = dat[5:6]
                if start_pipe == b'|' and end_pipe == b'|':
                    #Attempt to add to database
                    print('Standby: {}, Intensity: {}'.format(standby, intensity))
                    doc_ref = self.db.collection(u'devices').document(u'VLUaKArgEQ2oENfZs9WE')
                    doc_ref.update({u'standby': standby})
                    doc_ref.update({u'intensity': intensity})

            except IOError as e:
                print("IOError: ", e)
                time.sleep(2)
class Listeners:
    def __init__(self):
        logging.basicConfig()
        self.logger = logging.getLogger('logger')

        HOST = "localhost"

        DEVICE_SOCKET_PORT = 5555
        ALARM_SOCKET_PORT = 5556

        device_socket_not_connected = True
        while device_socket_not_connected:
            try:
                self.device_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.device_socket.connect((HOST, DEVICE_SOCKET_PORT))
                device_socket_not_connected = False
            except:
                print('connection refused')
                time.sleep(2)




        alarm_socket_not_connected = True
        while alarm_socket_not_connected:
            try:
                self.alarm_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.alarm_socket.connect((HOST, ALARM_SOCKET_PORT))
                alarm_socket_not_connected = False
            except:
                print('connection refused')
                time.sleep(2)

        self.REMOTE_SERVER = "www.google.co.uk"

        cred = credentials.Certificate('/home/pi/RGBProject/lightingproject-b4e13-firebase-adminsdk-s1ody-d6f69bf0cb.json')
        default_app = firebase_admin.initialize_app(cred)

        self.db = firestore.client()
        # now hook up receive data loop
        TcpListener(self.device_socket, self.db)
        self.start_snapshot_alarm()
        self.start_snapshot_device()

    def is_connected(self, hostname):
        try:
            host = socket.gethostbyname(hostname)
            s = socket.create_connection((host, 80), 2)
            s.close()
            return True
        except:
            pass
        return False

    def on_snapshot_device(self, doc_snapshot, changes, read_time):
        for doc in doc_snapshot:
            custom_effect_reference = doc.get('customEffect')
            custom_effect_document = custom_effect_reference.get()

            doc_dict = doc.to_dict()
            doc_dict.pop(u'customEffect')

            return_string = "{}|{}\n".format(json.dumps(doc_dict), json.dumps(custom_effect_document.to_dict()))
            self.device_socket.sendall(return_string)

    def on_snapshot_alarms(self, col_snapshot, changes, read_time):
        return_string = ""
        for doc in col_snapshot:
            alarm_json = json.dumps(doc.to_dict())
            return_string += "|{}".format(alarm_json)
        return_string += "\n"
        self.alarm_socket.sendall(return_string)

    def start_snapshot_alarm(self):
        while True:
            if self.is_connected("www.google.co.uk"):

                col_query = self.db.collection(u'users').document(u'pilton').collection(u'alarms')

                #watch document
                self.query_watch = col_query.on_snapshot(self.on_snapshot_alarms)
                break
            else:
                time.sleep(2)
                self.logger.warning('No Connection')

    def start_snapshot_device(self):
        while True:
            if self.is_connected("www.google.co.uk"):
                doc_ref = self.db.collection(u'devices').document(u'VLUaKArgEQ2oENfZs9WE')
                self.doc_watch = doc_ref.on_snapshot(self.on_snapshot_device)
                break
            else:
                time.sleep(2)
                self.logger.warning('No Connection')



if __name__ == '__main__':
    try:
        listeners_object = Listeners()
        while(True):
            if(listeners_object.query_watch._closed):
                listeners_object.start_snapshot_alarm()
            
            if(listeners_object.doc_watch._closed):
                listeners_object.start_snapshot_device()
            time.sleep(1)
    except Exception as err:
        print(err)
        print("Error occured at " + str(time.ctime()))
