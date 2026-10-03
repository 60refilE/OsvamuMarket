package py.edu.fpuna.entities;

public class Factura {
    private int idFactura;
    private int idCompra; // El ID que se generó en OsvamuMarket al momento de comprar
    private int montoTotal;
    private String fecha;

    public Factura() {
    }

    public Factura(int idFactura, int idCompra, int montoTotal, String fecha) {
        this.idFactura = idFactura;
        this.idCompra = idCompra;
        this.montoTotal = montoTotal;
        this.fecha = fecha;
    }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public int getIdCompra() { return idCompra; }
    public void setIdCompra(int idCompra) { this.idCompra = idCompra; }

    public int getMontoTotal() { return montoTotal; }
    public void setMontoTotal(int montoTotal) { this.montoTotal = montoTotal; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}