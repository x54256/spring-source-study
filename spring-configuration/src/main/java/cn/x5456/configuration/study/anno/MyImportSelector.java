package cn.x5456.configuration.study.anno;

import cn.x5456.configuration.study.beans.imports.ImportSelectorBean1;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class MyImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        if (importingClassMetadata.hasAnnotation(MyEnableAnnotation.class.getName())) {
            return new String[]{ImportSelectorBean1.class.getName()};
        }
        return new String[0];
    }
}
