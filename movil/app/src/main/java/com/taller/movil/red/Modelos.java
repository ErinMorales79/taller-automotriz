package com.taller.movil.red;

import java.util.Collections;
import java.util.List;

/** Datos que viajan por la API (ver docs/api.md). Son solo contenedores: la lógica vive en el servidor. */
public final class Modelos {

    private Modelos() {
    }

    public static final class LoginSolicitud {
        private final String usuario;
        private final String contrasena;

        public LoginSolicitud(String usuario, String contrasena) {
            this.usuario = usuario;
            this.contrasena = contrasena;
        }
    }

    public static final class LoginRespuesta {
        private String token;
        private String expiraEn;
        private String usuario;
        private String tecnico;

        public String getToken() {
            return token;
        }

        public String getExpiraEn() {
            return expiraEn;
        }

        public String getUsuario() {
            return usuario;
        }

        public String getTecnico() {
            return tecnico;
        }
    }

    public static final class AvanceSolicitud {
        private final int porcentaje;

        public AvanceSolicitud(int porcentaje) {
            this.porcentaje = porcentaje;
        }
    }

    public static final class ObservacionSolicitud {
        private final String texto;

        public ObservacionSolicitud(String texto) {
            this.texto = texto;
        }
    }

    public static final class Tarea {
        private int idAsignacion;
        private int idOrden;
        private String placa;
        private String vehiculo;
        private String tipoAtencion;
        private String prioridad;
        private String estadoOrden;
        private String estadoTarea;
        private int porcentajeAvance;
        private String fechaAsignacion;

        public int getIdAsignacion() {
            return idAsignacion;
        }

        public int getIdOrden() {
            return idOrden;
        }

        public String getPlaca() {
            return placa;
        }

        public String getVehiculo() {
            return vehiculo;
        }

        public String getTipoAtencion() {
            return tipoAtencion;
        }

        public String getPrioridad() {
            return prioridad;
        }

        public String getEstadoOrden() {
            return estadoOrden;
        }

        public String getEstadoTarea() {
            return estadoTarea;
        }

        public int getPorcentajeAvance() {
            return porcentajeAvance;
        }

        public String getFechaAsignacion() {
            return fechaAsignacion;
        }
    }

    public static final class Diagnostico {
        private String tecnico;
        private String descripcion;
        private String fecha;

        public String getTecnico() {
            return tecnico;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getFecha() {
            return fecha;
        }
    }

    public static final class Observacion {
        private int id;
        private String tecnico;
        private String texto;
        private String fecha;

        public int getId() {
            return id;
        }

        public String getTecnico() {
            return tecnico;
        }

        public String getTexto() {
            return texto;
        }

        public String getFecha() {
            return fecha;
        }
    }

    public static final class DetalleOrden {
        private int idOrden;
        private String placa;
        private String vehiculo;
        private int anio;
        private String color;
        private String tipoAtencion;
        private String prioridad;
        private String estado;
        private String fechaIngreso;
        private Tarea miTarea;
        private List<Diagnostico> diagnosticos;
        private List<Observacion> observaciones;

        public int getIdOrden() {
            return idOrden;
        }

        public String getPlaca() {
            return placa;
        }

        public String getVehiculo() {
            return vehiculo;
        }

        public int getAnio() {
            return anio;
        }

        public String getColor() {
            return color;
        }

        public String getTipoAtencion() {
            return tipoAtencion;
        }

        public String getPrioridad() {
            return prioridad;
        }

        public String getEstado() {
            return estado;
        }

        public String getFechaIngreso() {
            return fechaIngreso;
        }

        public Tarea getMiTarea() {
            return miTarea;
        }

        public List<Diagnostico> getDiagnosticos() {
            return diagnosticos == null ? Collections.emptyList() : diagnosticos;
        }

        public List<Observacion> getObservaciones() {
            return observaciones == null ? Collections.emptyList() : observaciones;
        }
    }
}
