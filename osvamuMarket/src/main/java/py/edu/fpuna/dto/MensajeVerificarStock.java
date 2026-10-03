package py.edu.fpuna.dto;

public class MensajeVerificarStock {
    private int idProducto;

    public MensajeVerificarStock(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
}