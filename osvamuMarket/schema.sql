-- Servicio 2 (Orden de Compra, TCP)
-- Tablas mínimas necesarias. Compatible con ProductoDAO existente.

CREATE TABLE IF NOT EXISTS productos (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    price    INTEGER NOT NULL CHECK (price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS compras (
    id    SERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total INTEGER NOT NULL CHECK (total >= 0)
);

CREATE TABLE IF NOT EXISTS detalle_compra (
    id              SERIAL PRIMARY KEY,
    compra_id       INTEGER NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    producto_id     INTEGER NOT NULL REFERENCES productos(id),
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario INTEGER NOT NULL CHECK (precio_unitario >= 0),
    subtotal        INTEGER NOT NULL CHECK (subtotal >= 0)
);
