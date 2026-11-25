package com.cappucare.app.reglas

import com.cappucare.app.model.CategoriaInsumo
import com.cappucare.app.model.EstadoProducto
import com.cappucare.app.model.MotivoSalida
import com.cappucare.app.model.Perfil
import com.cappucare.app.model.Producto
import com.cappucare.app.model.TipoMovimiento
import com.cappucare.app.model.UnidadMedida
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasCompletasResumenTest {

    private fun productoBase(
        id: String = "p1",
        codigo: String = "CUR-001",
        nombre: String = "Gasa estéril",
        cantidad: Int = 10,
        minimo: Int = 5,
        estado: EstadoProducto = EstadoProducto.DISPONIBLE
    ) = Producto(
        id = id,
        codigo = codigo,
        nombre = nombre,
        descripcion = "Insumo de prueba",
        categoria = CategoriaInsumo.CURACION,
        unidadMedida = UnidadMedida.UNIDAD,
        cantidadDisponible = cantidad,
        nivelMinimo = minimo,
        ubicacion = "Estante A1",
        estado = estado
    )

    @Test
    fun ingresoAumentaStockYRecuperaEstadoAgotado() {
        val agotado = productoBase(cantidad = 0, minimo = 4, estado = EstadoProducto.AGOTADO)
        val actualizado = ReglasInventario.aplicarMovimiento(
            producto = agotado,
            tipo = TipoMovimiento.INGRESO,
            cantidad = 6
        )
        assertEquals(6, actualizado.cantidadDisponible)
        assertEquals(EstadoProducto.DISPONIBLE, actualizado.estado)
    }

    @Test
    fun salidaHastaCeroMarcaAgotadoYCruzaElMinimo() {
        val disponible = productoBase(cantidad = 5, minimo = 4, estado = EstadoProducto.DISPONIBLE)
        val actualizado = ReglasInventario.aplicarMovimiento(
            producto = disponible,
            tipo = TipoMovimiento.SALIDA,
            cantidad = 5
        )
        assertEquals(0, actualizado.cantidadDisponible)
        assertEquals(EstadoProducto.AGOTADO, actualizado.estado)
        assertTrue(ReglaReposicion.cruzoElMinimo(disponible, actualizado))
    }

    @Test
    fun salidaMayorAlStockEsRechazadaPorValidacion() {
        val producto = productoBase(cantidad = 3, minimo = 2)
        val errores = Validaciones.validarMovimiento(
            producto = producto,
            tipo = TipoMovimiento.SALIDA,
            cantidad = "4",
            motivo = MotivoSalida.VENTA
        )
        assertTrue(errores.hayErrores)
        assertTrue(errores.cantidad?.contains("3") == true)
    }

    @Test
    fun productoDadoDeBajaNoEntraEnAlertasDeReposicion() {
        val dadoDeBaja = productoBase(
            id = "p2",
            codigo = "MED-099",
            nombre = "Insumo dado de baja",
            cantidad = 0,
            minimo = 10,
            estado = EstadoProducto.DADO_DE_BAJA
        )
        assertFalse(ReglaReposicion.estaBajoMinimo(dadoDeBaja))
    }

    @Test
    fun perfilNoEncargadoNoPuedeEditarCatalogo() {
        assertFalse(Perfil.USUARIO_AUTORIZADO.puedeEditarCatalogo)
        assertTrue(Perfil.ENCARGADO.puedeEditarCatalogo)
    }
}
