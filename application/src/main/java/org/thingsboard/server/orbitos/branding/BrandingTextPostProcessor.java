// SPDX-FileCopyrightText: Copyright The OrbitOS Authors
// SPDX-License-Identifier: Apache-2.0
package org.thingsboard.server.orbitos.branding;

import freemarker.template.TemplateScalarModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Puts the brand name into server-side texts without editing upstream code:
 * - email templates (FreeMarker) get a shared variable ${brandName};
 * - messages (e.g. email subjects) have "{brandName}" replaced.
 * The name is looked up lazily, so this works before BrandingService is ready.
 */
@Component
public class BrandingTextPostProcessor implements BeanPostProcessor {

    static final String PLACEHOLDER = "{brandName}";

    private final ObjectProvider<BrandingService> brandingService;

    public BrandingTextPostProcessor(ObjectProvider<BrandingService> brandingService) {
        this.brandingService = brandingService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof freemarker.template.Configuration freemarker) {
            TemplateScalarModel brandName = this::brandName;
            freemarker.setSharedVariable("brandName", brandName);
        } else if (bean instanceof MessageSource messageSource && "messageSource".equals(beanName)) {
            return new BrandedMessageSource(messageSource);
        }
        return bean;
    }

    String brandName() {
        BrandingService service = brandingService.getIfAvailable();
        return service != null ? service.getBranding().name() : BrandingService.DEFAULT_NAME;
    }

    String brand(String message) {
        return message != null && message.contains(PLACEHOLDER) ? message.replace(PLACEHOLDER, brandName()) : message;
    }

    private class BrandedMessageSource implements MessageSource {

        private final MessageSource delegate;

        BrandedMessageSource(MessageSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
            return brand(delegate.getMessage(code, args, defaultMessage, locale));
        }

        @Override
        public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
            return brand(delegate.getMessage(code, args, locale));
        }

        @Override
        public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
            return brand(delegate.getMessage(resolvable, locale));
        }
    }

}
