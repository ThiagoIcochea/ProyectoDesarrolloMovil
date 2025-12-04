#!/usr/bin/env python3
from datetime import datetime, timedelta, date
import csv

# Replica de la lógica de calcularResumen en Python para probar

DATE_FMT = "%Y-%m-%d"
DATETIME_FMT = "%Y-%m-%d %H:%M:%S"

def parse_date_flexible(s):
    if not s: return None
    for fmt in ["%Y-%m-%dT%H:%M:%S","%Y-%m-%d %H:%M:%S","%Y-%m-%d"]:
        try:
            return datetime.strptime(s, fmt)
        except:
            pass
    return None

def working_dates_between(start, end):
    if start is None or end is None: return []
    start_date = start.date() if isinstance(start, datetime) else start
    end_date = end.date() if isinstance(end, datetime) else end
    d = start_date
    res = []
    while d <= end_date:
        if d.weekday() < 5: # Mon-Fri
            res.append(d)
        d += timedelta(days=1)
    return res

# Data classes (simple dicts)
# Sample data
usuarios = [
    { 'usuarioId': 1, 'personal': { 'idPersonal': 1, 'nombre': 'Admin', 'apellPaterno': 'Uno', 'apellMaterno': '' , 'nroDocumento': 'A001' }, 'fechaCreacion': '2025-10-01' },
    { 'usuarioId': 2, 'personal': { 'idPersonal': 2, 'nombre': 'Empleado', 'apellPaterno': 'Dos', 'apellMaterno': '' , 'nroDocumento': 'E002' }, 'fechaCreacion': '2025-09-01' },
    { 'usuarioId': 3, 'personal': { 'idPersonal': 3, 'nombre': 'Nuevo', 'apellPaterno': 'Tres', 'apellMaterno': '' , 'nroDocumento': 'N003' }, 'fechaCreacion': '2025-12-03' }
]

# Asistencias: admin (id 1) and employee (id 2)
asistencias = [
    # Admin: varios dias con entradas y salidas
    { 'personal': { 'idPersonal': 1 }, 'movimiento': { 'descripcion': 'Entrada', 'abreDesc': 'ENT' }, 'fecha': '2025-11-10 08:05:00' },
    { 'personal': { 'idPersonal': 1 }, 'movimiento': { 'descripcion': 'Salida', 'abreDesc': 'SAL' }, 'fecha': '2025-11-10 17:00:00' },
    { 'personal': { 'idPersonal': 1 }, 'movimiento': { 'descripcion': 'Entrada', 'abreDesc': 'ENT' }, 'fecha': '2025-11-11 08:20:00' },
    # admin only entry (no exit) on 2025-11-12
    { 'personal': { 'idPersonal': 1 }, 'movimiento': { 'descripcion': 'Entrada', 'abreDesc': 'ENT' }, 'fecha': '2025-11-12 08:10:00' },

    # Empleado 2: muchas entradas/salidas
    { 'personal': { 'idPersonal': 2 }, 'movimiento': { 'descripcion': 'Entrada', 'abreDesc': 'ENT' }, 'fecha': '2025-11-10 08:50:00' },
    { 'personal': { 'idPersonal': 2 }, 'movimiento': { 'descripcion': 'Salida', 'abreDesc': 'SAL' }, 'fecha': '2025-11-10 17:05:00' },
    { 'personal': { 'idPersonal': 2 }, 'movimiento': { 'descripcion': 'Entrada', 'abreDesc': 'ENT' }, 'fecha': '2025-11-11 07:55:00' },
    { 'personal': { 'idPersonal': 2 }, 'movimiento': { 'descripcion': 'Salida', 'abreDesc': 'SAL' }, 'fecha': '2025-11-11 16:50:00' },
]

PERIODO_INICIO = '2025-10-01'
PERIODO_FIN = datetime.now().strftime(DATE_FMT)

LIMIT_TARDANZA = 8*60 + 15


def calcular_resumen_py(asistencias, usuarios, periodo_inicio, periodo_fin):
    sdf = DATE_FMT
    inicioDate = parse_date_flexible(periodo_inicio)
    finDate = parse_date_flexible(periodo_fin)
    # clamp finDate to today
    today = datetime.combine(date.today(), datetime.min.time())
    if finDate is None or finDate.date() > today.date():
        finDate = today

    # build personal map
    personal_map = {}
    for u in usuarios:
        p = u.get('personal')
        if p and p.get('idPersonal') is not None:
            personal_map[p['idPersonal']] = p
    for a in asistencias:
        p = a.get('personal')
        if p and p.get('idPersonal') is not None and p['idPersonal'] not in personal_map:
            personal_map[p['idPersonal']] = p

    # group asistencias by personal
    asist_by_personal = {}
    for a in asistencias:
        pid = a.get('personal', {}).get('idPersonal')
        if pid is None: continue
        asist_by_personal.setdefault(pid, []).append(a)

    rows = []
    for pid, personal in personal_map.items():
        registros = asist_by_personal.get(pid, [])
        registros_por_fecha = {}
        for r in registros:
            f = r.get('fecha')
            if not f: continue
            fecha_only = f[:10]
            registros_por_fecha.setdefault(fecha_only, []).append(r)

        # fechaCreacion +1
        usuario = next((u for u in usuarios if u.get('personal', {}).get('idPersonal') == pid), None)
        inicio_ef = inicioDate
        if usuario and usuario.get('fechaCreacion'):
            parsed = parse_date_flexible(usuario['fechaCreacion'])
            if parsed:
                inicio_by_user = (parsed + timedelta(days=1))
                if inicioDate is None or inicio_by_user > inicioDate:
                    inicio_ef = inicio_by_user
        if inicio_ef is None:
            # if still none, skip
            inicio_ef = parse_date_flexible('2025-10-01')

        # clamp fin per user to last marca if exists
        # compute last marca for this pid
        last_marca = None
        # find last marca (max fecha) in registros_por_fecha
        if registros_por_fecha:
            try:
                last_marca_str = max(registros_por_fecha.keys())
                last_marca = datetime.strptime(last_marca_str, DATE_FMT)
            except:
                last_marca = None

        fin_user = finDate
        if last_marca is not None and last_marca.date() < finDate.date():
            # set fin to last_marca date
            fin_user = datetime.combine(last_marca.date(), datetime.min.time())

        dias_lab = working_dates_between(inicio_ef, fin_user)
        dias_lab_str = set([d.strftime(DATE_FMT) for d in dias_lab])

        fechas_con_marca = set([f for f in registros_por_fecha.keys()])
        dias_presentes = fechas_con_marca.intersection(dias_lab_str)

        demoras = 0
        for dia in dias_presentes:
            marcas = registros_por_fecha.get(dia, [])
            # prefer entrada
            entry = [m for m in marcas if (m.get('movimiento', {}).get('descripcion','').lower().find('entrada')>=0) or (m.get('movimiento', {}).get('abreDesc','').upper()=='ENT')]
            chosen = None
            if entry:
                chosen = min(entry, key=lambda x: x.get('fecha',''))
            else:
                chosen = min(marcas, key=lambda x: x.get('fecha',''))
            if chosen and chosen.get('fecha'):
                t = parse_date_flexible(chosen.get('fecha'))
                if t:
                    minutos = t.hour*60 + t.minute
                    if minutos > LIMIT_TARDANZA:
                        demoras += 1
        total_asist = len(dias_presentes)
        faltas = len(dias_lab_str) - total_asist
        descuento = faltas*5.0 + demoras*2.0
        nombre = ' '.join(filter(None,[personal.get('nombre'), personal.get('apellPaterno'), personal.get('apellMaterno')])) or 'Sin nombre'
        doc = personal.get('nroDocumento')
        ultima = max([r.get('fecha') for r in registros], default=None)
        rows.append({'nombre': nombre, 'documento': doc, 'asistencias': total_asist, 'demoras': demoras, 'faltas': faltas, 'descuento': descuento, 'ultima': ultima})

    return rows

if __name__ == '__main__':
    rows = calcular_resumen_py(asistencias, usuarios, PERIODO_INICIO, PERIODO_FIN)
    print('Resultados de prueba:')
    for r in rows:
        print(r)
    # export CSV local
    with open('/workspaces/ProyectoDesarrolloMovil/artifacts/test_report.csv','w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(['Nombre','Documento','Asistencias','Demoras','Faltas','Descuento','UltimaMarca'])
        for r in rows:
            writer.writerow([r['nombre'], r['documento'] or '', r['asistencias'], r['demoras'], r['faltas'], '{:.1f}'.format(r['descuento']), r['ultima'] or ''])
    print('\nCSV escrito en /workspaces/ProyectoDesarrolloMovil/artifacts/test_report.csv')
