import cherrypy, os, inspect, json
from server_data import *

class BSICRM:
    @cherrypy.expose
    def index(self, **args):
        return """
<!DOCTYPE html>
<html>
	<head>
		<title>BSI CRM 2014</title>
		<meta content="text/html; charset=UTF-8" http-equiv="content-type">
		<link rel="stylesheet" type="text/css" href="static/scout.css">
		<link rel="shortcut icon" type="image/x-icon" href="static/favicon.ico">
		<script src="static/jquery-2.0.3.min.js"></script>
		<script src="static/scout.js"></script>
	</head>
	<body>
		<div id="Desktop" class="scout"></div>
	</body>
</html>"""


    @cherrypy.expose
    def model(self, id=None, **args):
        if id == 'Desktop':
            message = [{'type': 'desktop',
                        'start': 'v1',
                        'views': [{'id': 'v1', 'label': 'Eigene Sicht', 'state': 'selected'},
                                  {'id': 'v2', 'label': 'Standardsicht'},
                                  {'id': 'v3', 'label': 'Marketing'},
                                  {'id': 'v4', 'label': 'Contact Center'},
                                  {'id': 'v5', 'label': 'Reports'},
                                  {'id': 'v6', 'label': 'Administration'}],
                        'tools': [{'id': 't1', 'label': 'Suche', 'icon': '\uf002', 'shortcut': 'F3'},
                                  {'id': 't2', 'label': 'Zugriff', 'icon': '\uf144', 'shortcut': 'F4'},
                                  {'id': 't3', 'label': 'Favoriten', 'icon': '\uf005', 'shortcut': 'F6'},
                                  {'id': 't4', 'label': 'Muster', 'icon': '\uf01C', 'shortcut': 'F7', 'state': 'disabled'},
                                  {'id': 't5', 'label': 'Telefon', 'icon': '\uf095', 'shortcut': 'F8'},
                                  {'id': 't6', 'label': 'Cockpit', 'icon': '\uf0E4', 'shortcut': 'F9'},
                                  {'id': 't7', 'label': 'Prozesse', 'icon': '\uf0D0','shortcut': 'F10'},
                                  {'id': 't8', 'label': 'Offline', 'icon': '\uf0D0','shortcut': 'F10'}]}] 
        else:
            message = ''
                               
        return json.dumps(message)

    @cherrypy.expose
    def drilldown(self, id=None, **args):
        # todo: one tree, sear id and children
        if id[0] == 'v':
            message = [{'id': 'n1', 'label': 'Christian Rusche (BSI Baden)', 'state': 'can-expand expanded selected',
                        'bench': {'type': 'form'},
                        'children': [
                            {'id': 'n2', 'label': 'Firmen', 'state': 'can-expand',
                             'bench': {'type': 'table', 'id': 'n2', 'columns': COMPANY_COLUMN, 'chart': 'Diagramm', 'graph': 'Netzwerk', 'map': 'Karte'}},
                            {'id': 'n3', 'label': 'Aufträge', 'state': 'can-expand',
                             'bench': {'type': 'table', 'id': 'n3', 'columns': BUSINESS_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': 'Karte'}},
                            {'id': 'n4', 'label': 'Aufgaben',
                             'bench': {'type': 'table', 'id': 'n4', 'columns': TODO_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': ''}},
                            {'id': 'n5', 'label': 'Kommunikation',
                             'bench': {'type': 'table', 'id': 'n5', 'columns': COMMUNICATION_COLUMN, 'chart': 'Diagramm', 'off': 'load', 'map': 'Karte'}}
                             ]}]

        elif id == 'n1':
            message = [{'id': 'n2', 'label': 'Firmen', 'state': 'can-expand',
                          'bench': {'type': 'table', 'id': 'n2', 'columns': COMPANY_COLUMN, 'chart': 'Diagramm', 'graph': 'Netzwerk', 'map': 'Karte'}},
                        {'id': 'n3', 'label': 'Aufträge', 'state': 'can-expand',
                           'bench': {'type': 'table', 'id': 'n2', 'columns': BUSINESS_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': 'Karte'}},
                        {'id': 'n4', 'label': 'Aufgaben',
                           'bench': {'type': 'table', 'id': 'n4', 'columns': TODO_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': ''}},
                        {'id': 'n5', 'label': 'Kommunikation',
                           'bench': {'type': 'table', 'id': 'n5', 'columns': COMMUNICATION_COLUMN, 'chart': 'Diagramm', 'off': 'load', 'map': 'Karte'}}]


        elif 'n2' in id:
            message = [{'id': 'n6', 'label': 'ROLAND TECHNIK', 'state': 'can-expand expanded',
                        'bench': {'type': 'form'},
                        'children': [
                            {'id': 'n31', 'label': 'Aufträge', 'state': 'can-expand',
                             'bench': {'type': 'table', 'id': 'n3', 'columns': BUSINESS_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': 'Karte'}},
                            {'id': 'n41', 'label': 'Aufgaben',
                             'bench': {'type': 'table', 'id': 'n4', 'columns': TODO_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': ''}},
                            {'id': 'n51', 'label': 'Kommunikation',
                             'bench': {'type': 'table', 'id': 'n5', 'columns': COMPANY_COLUMN, 'chart': 'Diagramm', 'graph': 'Netzwerk', 'map': 'Karte'}}
                            ]}]
 
        elif 'n3' in id:
            message = [{'id': 'n7', 'label': 'BSI CTMS ROCHE', 'state': 'can-expand expanded',
                        'bench': {'type': 'form'},
                        'children': [
                            {'id': 'n22', 'label': 'Firmen', 'state': 'can-expand',
                             'bench': {'type': 'table', 'id': 'n2', 'columns': COMPANY_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': 'Karte'}},
                            {'id': 'n42', 'label': 'Aufgaben',
                             'bench': {'type': 'table', 'id': 'n4', 'columns': TODO_COLUMN, 'chart': 'Diagramm', 'graph': '', 'map': ''}},
                            {'id': 'n52', 'label': 'Kommunikation',
                             'bench': {'type': 'table', 'id': 'n5', 'columns': COMPANY_COLUMN, 'chart': 'Diagramm', 'graph': 'Netzwerk', 'map': 'Karte'}}
                            ]}]
        elif 'n4' in id:
            message = [{'id': 'n7', 'label': 'Kunde anrufen', 'state': '',
                        'bench': {'type': 'form'}}]

        elif 'n5' in id:
            message = [{'id': 'n8', 'label': 'Besuch in Berlin', 'state': '',
                        'bench': {'type': 'form'}}]

        else:
            message = ''
                               
        return json.dumps(message)

    @cherrypy.expose
    def drilldown_data(self, id=None, **args):
        if id == 'n2':
            message = COMPANY
        elif id == 'n3':
            message = BUSINESS * 10
        elif id == 'n4':
            message = TODO
        elif id == 'n5':
            message = COMMUNICATION
 
        return json.dumps(message)

    @cherrypy.expose
    def drilldown_graph(self, id=None, **args):
        message = GRAPH 

        return json.dumps(message)

    @cherrypy.expose
    def drilldown_map(self, id=None, **args):
        message = MAP 

        return json.dumps(message)


    @cherrypy.expose
    def drilldown_menu(self, id=None, **args):
        if id == 'n1':
            message = []
            
        elif 'n2' in id:
            message = [{'id': 'm1', 'label': 'Neue Firma anlegen'}]

        elif 'n3' in id:
            message = [{'id': 'm2', 'label': 'Neuer Auftrag anlegen',
                        'children' : [{'id': 'm3', 'label': 'Fixpreis'},
                                      {'id': 'm4', 'label': 'Lizenz'},
                                      {'id': 'm5', 'label': 'Wartung'}]}]

        elif 'n4' in id:
            message = [{'id': 'm6', 'label': 'Neuer Aufgabe anlegen'}]

        elif 'n5' in id:
            message = [{'id': 'm7', 'label': 'Neue Kommunikation anlegen'}]
        
        elif 'n6' in id:
            message = [{'id': 'm8', 'label': 'Anrufen', 'icon': '\uf095'},
                       {'id': 'm9', 'label': 'E-Mail schreiben', 'icon': '\uf0E0'},
                       {'id': 'm10', 'label': 'Dokument erstellen', 'icon': '\uf15C'},
                       {'id': 'm11', 'label': 'Firmen-Dossier', 'icon': '\uf02D'},
                       {'id': 'm12', 'label': 'Firma bearbeiten'},
                       {'id': 'm13', 'label': 'Firma zusammenlegen'}]

        elif 'n7' in id:
            message = [{'id': 'm14', 'label': 'Dokument erstellen', 'icon': '\uf15C'},
                       {'id': 'm15', 'label': 'Auftrag bearbeiten'}]

        elif 'n8' in id:
            message = [{'id': 'm16', 'label': 'Anrufen', 'icon': '\uf095'},
                       {'id': 'm17', 'label': 'E-Mail schreiben', 'icon': '\uf0E0'},
                       {'id': 'm18', 'label': 'Dokument erstellen', 'icon': '\uf15C'},
                       {'id': 'm19', 'label': 'Kunden-Dossier', 'icon': '\uf02D'},
                       {'id': 'm20', 'label': 'Kommunikation bearbeiten'},
                       {'id': 'm21', 'label': 'Kommunikation löschen', 'shortcut': 'del'}]

        elif 'n9' in id:
            message = [{'id': 'm22', 'label': 'Anrufen', 'icon': '\uf095'},
                       {'id': 'm23', 'label': 'E-Mail schreiben', 'icon': '\uf0E0'},
                       {'id': 'm24', 'label': 'Dokument erstellen', 'icon': '\uf15C'},
                       {'id': 'm25', 'label': 'Kunden-Dossier', 'icon': '\uf02D'},
                       {'id': 'm26', 'label': 'Kommunikation bearbeiten'},
                       {'id': 'm27', 'label': 'Kommunikation löschenn', 'shortcut': 'del'}]

        return json.dumps(message)


current_dir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
conf = {
    'global': {'server.socket_host': '127.0.0.1',
               'server.socket_port': 15547},
    '/static':{'tools.staticdir.on': True,
               'tools.staticdir.dir': os.path.join(current_dir, 'static')}}

cherrypy.quickstart(root=BSICRM(), config=conf)
