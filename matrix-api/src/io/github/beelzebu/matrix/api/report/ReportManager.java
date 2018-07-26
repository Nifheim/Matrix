package io.github.beelzebu.matrix.api.report;

import java.util.Optional;

/**
 * @author Beelzebu
 */
public interface ReportManager {

    /**
     * Obtiene un reporte en base a su id
     *
     * @param id id del reporte para buscar en la base de datos.
     * @return repote obtenido desde la base de datos.
     */
    Optional<Report> getReport(int id);

    /**
     * Crea un reporte en la base de datos y lo obtiene con la nueva id.
     *
     * @param report reporte para crear en la base de datos.
     * @return reporte con la nueva id.
     * @throws IllegalArgumentException si la id no es -1
     */
    Report createReport(Report report) throws IllegalArgumentException;

}
