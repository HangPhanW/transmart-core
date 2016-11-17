package org.transmartproject.db.multidimquery

import com.google.common.collect.AbstractIterator
import com.google.common.collect.ImmutableList
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.TailRecursive
import groovy.transform.TupleConstructor
import org.transmartproject.core.dataquery.DataColumn
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.multidimquery.Dimension
import org.transmartproject.core.multidimquery.Hypercube
import org.transmartproject.core.multidimquery.HypercubeValue
import org.transmartproject.core.multidimquery.dimensions.BioMarker
import org.transmartproject.db.metadata.DimensionDescription
import org.transmartproject.db.util.AbstractOneTimeCallIterable
import org.transmartproject.db.util.IndexedArraySet

@CompileStatic
class HddTabularResultHypercubeAdapter extends AbstractOneTimeCallIterable<HypercubeValue> implements Hypercube {
    private static Object typeError(cell) {
        throw new RuntimeException("HDD value $cell is not a Double and is not a Map, this projection is not" +
                " implemented in HddTabularResultHypercubeAdapter")
    }

    static Dimension biomarkerDim = DimensionDescription.dimensionsMap.biomarker
    static Dimension assayDim = DimensionDescription.dimensionsMap.assay
    static Dimension patientDim = DimensionDescription.dimensionsMap.patient
    static Dimension projectionDim = DimensionDescription.dimensionsMap.projection

    TabularResult<AssayColumn, ? extends DataRow<AssayColumn, ? /* depends on projection */>> table
    private TabularResultAdapterIterator iterator

    // Either an IndexedArraySet or an ImmutableList
    private List<String> _projectionFields = null
    List<String> getProjectionFields() {
        if(_projectionFields != null) return _projectionFields
        getIterator().hasNext() // sets _projectionFields as a side effect
        _projectionFields
    }

    @Lazy ImmutableList<Dimension> dimensions = (
        !projectionFields
            ? ImmutableList.of(biomarkerDim, assayDim, patientDim)
            : ImmutableList.of(biomarkerDim, assayDim, patientDim, projectionDim)
    )

    ImmutableList<Assay> assays
    ImmutableList<Patient> patients
    List<BioMarker> biomarkers = [] // replaced by an ImmutableList once we have finished iterating

    HddTabularResultHypercubeAdapter(TabularResult<AssayColumn, ? extends DataRow<AssayColumn, ?>> tabularResult) {
        table = tabularResult
        assays = (ImmutableList) ImmutableList.copyOf(table.getIndicesList())
        patients = (ImmutableList) ImmutableList.copyOf(assays*.patient)
    }

    protected List<? extends Object> _dimensionElems(Dimension dim) {
        if(dim == assayDim) return assays
        else if(dim == patientDim) return patients
        else if(dim == biomarkerDim) return biomarkers
        else if(dim == projectionDim && projectionFields != null) {
            return ImmutableList.copyOf(projectionFields)
        } else return null
    }

    ImmutableList<? extends Object> dimensionElements(Dimension dim) {
        List<? extends Object> elements = _dimensionElems(dim)
        // ImmutableList.copyOf is smart about not making copies of immutable lists
        elements == null ? null : ImmutableList.copyOf(elements)
    }

    Object dimensionElement(Dimension dim, Integer idx) {
        _dimensionElems(dim)[idx]
    }

    String dimensionElementKey(Dimension dim, Integer idx) {
        def elem = dimensionElement(dim, idx)
        if(elem instanceof String) return elem
        else if(elem instanceof DataColumn) return ((DataColumn) elem).label
        else if(elem instanceof Patient) return ((Patient) elem).inTrialId
        else throw new RuntimeException("unexpected element type ${elem.class}. Expected a String, Patient, or Assay")
    }


    @Override Iterator<HypercubeValue> getIterator() {
        iterator == null ? (iterator = new TabularResultAdapterIterator()) : iterator
    }

    class TabularResultAdapterIterator extends AbstractIterator<HypercubeValue> {
        private Iterator<? extends DataRow<AssayColumn, ?>> tabularIter = table.getRows()
        private List<HypercubeValue> nextVals = []
        private Iterator<HypercubeValue> nextIter = nextVals.iterator()
        TabularResultAdapterIterator() {
            if(_projectionFields == null) _projectionFields = new IndexedArraySet<String>()
        }

        @TailRecursive
        HypercubeValue computeNext() {
            if(nextIter.hasNext()) {
                return nextIter.next()
            }
            nextVals.clear()
            nextIter = null

            if(!tabularIter.hasNext()) {
                _projectionFields = ImmutableList.copyOf(_projectionFields)
                biomarkers = ImmutableList.copyOf(biomarkers)
                return endOfData()
            }

            DataRow<AssayColumn, ?> row = tabularIter.next()
            BioMarker bm = new BioMarkerAdapter(row.label,
                    row instanceof BioMarkerDataRow ? ((BioMarkerDataRow) row).bioMarker : null)
            int biomarkerIdx = biomarkers.size()
            biomarkers << bm

            // assays.size() compiles to GroovyDefaultMethods.size(Iterable) :(
            for(int i = 0; i < ((List)assays).size(); i++) {
                Assay assay = assays[i]
                def value = row[i]

                if(value instanceof Double) {
                    nextVals.add(new TabularResultAdapterValue(
                            // The type checker doesn't like a plain 'dimensions', no idea why
                            getDimensions(), value, bm, assay, null,
                            biomarkerIdx, i, -1
                    ))
                } else if(value instanceof Map) {
                    Map<String, Object> mapValue = (Map<String,Object>) value
                    Map.Entry<String, Object> entry
                    for(Iterator<Map.Entry> it = mapValue.iterator(); it.hasNext(); entry = it.next()) {
                        _projectionFields.add(entry.key)
                        nextVals.add(new TabularResultAdapterValue(
                                getDimensions(), entry.value, bm, assay, entry.key,
                                biomarkerIdx, i, _projectionFields.indexOf(entry.key)
                        ))
                    }
                } else {
                    typeError(value)
                }
            }

            nextIter = nextVals.iterator()
            return computeNext()
        }
    }

    @TupleConstructor
    static class TabularResultAdapterValue implements HypercubeValue {
        static int dimError(dim) {
            throw new IndexOutOfBoundsException("Dimension $dim is not applicable to this hypercube result")
        }

        ImmutableList<Dimension> availableDimensions
        def value
        BioMarker biomarker
        Assay assay
        String projectionKey
        int biomarkerIndex
        int assayIndex
        int projectionIndex

        Patient getPatient() { assay.patient }

        def getAt(Dimension dim) {
            if(dim == biomarkerDim) return biomarker
            if(dim == assayDim) return assay
            if(dim == patientDim) return patient
            if(dim == projectionDim && projectionKey) return projectionKey
            dimError(dim)
        }

        int getDimElementIndex(Dimension dim) {
            if(dim == biomarkerDim) return biomarkerIndex
            if(dim == assayDim) return assayIndex
            if(dim == patientDim) return assayIndex
            if(dim == projectionDim && projectionKey) return projectionIndex
            dimError(dim)
        }

        def getDimKey(Dimension dim) {
            if(dim == biomarkerDim) return biomarker.bioMarker ?: biomarker.label
            if(dim == assayDim) return assay.sampleCode
            if(dim == patientDim) return patient.inTrialId
            if(dim == projectionDim && projectionKey) return projectionKey
            dimError(dim)
        }
    }

    @Immutable
    static class BioMarkerAdapter implements BioMarker {
        final String label
        final String bioMarker
    }


    void loadDimensions() { /*no-op*/ }
    void preloadDimensions() { throw new UnsupportedOperationException() }
    final boolean dimensionsPreloadable = false
    final boolean dimensionsPreloaded = false
    boolean autoloadDimensions = true

    void close() {
        table.close()
    }
}
