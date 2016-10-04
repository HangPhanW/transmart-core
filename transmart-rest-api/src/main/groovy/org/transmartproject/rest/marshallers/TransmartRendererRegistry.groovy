package org.transmartproject.rest.marshallers

import grails.util.Holders
import org.grails.plugins.web.rest.render.DefaultRendererRegistry
import org.transmartproject.rest.misc.ComponentIndicatingContainer

/**
 * Customized the {@link DefaultRendererRegistry} by making it aware of the
 * {@link ComponentIndicatingContainer}.
 */
class TransmartRendererRegistry extends DefaultRendererRegistry {

    @Override
    void initialize() {
        modelSuffix = Holders.config.getAt('grails.scaffolding.templates.domainSuffix') ?: ''
        super.initialize()
    }

    @Override
    protected Class<? extends Object> getTargetClassForContainer(
            Class containerClass, Object object) {
        if (object instanceof ComponentIndicatingContainer) {
            return object.componentType
        }

        super.getTargetClassForContainer(containerClass, object)
    }
}
