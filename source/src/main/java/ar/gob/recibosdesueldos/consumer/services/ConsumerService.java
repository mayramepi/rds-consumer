package ar.gob.recibosdesueldos.consumer.services;

import ar.gob.recibosdesueldos.consumer.dao.ConsumerDao;
import ar.gob.recibosdesueldos.model.recibos.Recibo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ConsumerService {
    @Autowired
    private ConsumerDao dao;

    public void createRecibos(Recibo recibo) {
        this.dao.createRecibos(recibo);
    }

    public void updateEstadoRecibo(Long idReciboSial, String idLote) {
        this.dao.updateEstadoRecibo(idReciboSial, idLote);
    }

    public void updateEstadoReciboSial(Long idReciboSial, String idLote, String estado) {
        this.dao.updateEstadoReciboSial(idReciboSial, idLote, estado);
    }
}
