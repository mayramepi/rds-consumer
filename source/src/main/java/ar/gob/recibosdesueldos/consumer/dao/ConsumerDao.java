package ar.gob.recibosdesueldos.consumer.dao;

import ar.gob.recibosdesueldos.model.recibos.Recibo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class ConsumerDao {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String INSERT_RECIBO =
            "insert into RECIBOS(ID_LOTE, ID_PLANTILLA, PERIODO_MES, PERIODO_ANIO, CUIL, APELLIDO_NOMBRE, F_CREACION, ID_RECIBO_SIAL, COD_GRUPO, CONTENIDO_JSON, ESTADO)" +
            "       values(:idLote, (SELECT ID_PLANTILLA FROM GRUPOS WHERE COD_GRUPO = :codGrupo), :periodoMes, :periodoAnio, :cuil, :apellidoNombre, :fechaCreacion, :idReciboSial, :codGrupo, :contenidoJson, 'INICIADO')";
    private static final String UPDATE_ESTADO_RECIBO =
            "update RECIBOS set ESTADO = 'GENERADO' where ID_RECIBO_SIAL = :idReciboSial and ID_LOTE = :idLote";
    private static final String UPDATE_ESTADO_RECIBOS_SIAL =
            "update RECIBOS_SIAL set ESTADO = :estado where ID_RECIBO = :idReciboSial and ID_LOTE = :idLote";

    public void createRecibos(Recibo recibo) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                                                .addValue("idLote", recibo.getIdLote())
                                                .addValue("periodoMes", recibo.getPeriodoMes())
                                                .addValue("periodoAnio", recibo.getPeriodoAnio())
                                                .addValue("cuil", recibo.getCuil())
                                                .addValue("apellidoNombre", recibo.getApellidoNombre())
                                                .addValue("fechaCreacion", new Date())
                                                .addValue("idReciboSial", recibo.getIdReciboSial())
                                                .addValue("codGrupo", recibo.getCodigoGrupo())
                                                .addValue("contenidoJson", recibo.getContenidoJson().getBytes());
        namedParameterJdbcTemplate.update(INSERT_RECIBO, parameters);
    }

    public void updateEstadoRecibo(Long idReciboSial, String idLote) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                                                .addValue("idReciboSial", idReciboSial)
                                                .addValue("idLote", idLote);
        namedParameterJdbcTemplate.update(UPDATE_ESTADO_RECIBO, parameters);
    }

    public void updateEstadoReciboSial(Long idReciboSial, String idLote, String estado) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                                                .addValue("idReciboSial", idReciboSial)
                                                .addValue("idLote", idLote)
                                                .addValue("estado", estado);
        namedParameterJdbcTemplate.update(UPDATE_ESTADO_RECIBOS_SIAL, parameters);
    }
}
