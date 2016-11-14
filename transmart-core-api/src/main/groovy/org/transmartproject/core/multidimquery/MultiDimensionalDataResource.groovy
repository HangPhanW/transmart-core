package org.transmartproject.core.multidimquery

interface MultiDimensionalDataResource {

    /**
     *
     *
     * @param dataType: The string identifying the data type. "clinical" for clinical data, for high dimensional data
     * the appropriate identifier string.
     * @param constraints: (nullable) A list of Constraint-s. If null, selects all the data in the database.
     * @param dimensions: (nullable) A list of Dimension-s to select. Only dimensions valid for the selected studies
     * will actually be applied. If null, select all available dimensions.
     *
     * Not yet implemented:
     * @param sort
     * @param pack
     * @param preloadDimensions
     *
     * @return a Hypercube result
     */
    Hypercube retrieveData(Map args, String dataType)

    Dimension getDimension(String name)

}