package py.edu.fpuna.dto;

public class MensajeRastreoLogistico {
    private int codigoConfirmacion;

    public MensajeRastreoLogistico(int codigoConfirmacion) {
        this.codigoConfirmacion = codigoConfirmacion;
    }

    public int getCodigoConfirmacion() { return codigoConfirmacion; }
    public void setCodigoConfirmacion(int codigoConfirmacion) { this.codigoConfirmacion = codigoConfirmacion; }
}
