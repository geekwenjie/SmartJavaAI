/*
 * Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package cn.smartjavaai.instanceseg.translator;

import ai.djl.Model;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.ObjectDetectionTranslatorFactory;
import ai.djl.translate.Translator;

import java.io.Serializable;
import java.util.Map;

/** A translatorFactory that creates a {@link ai.djl.modality.cv.translator.YoloSegmentationTranslator} instance. */
public class YoloSegmentationTranslatorFactory2 extends ObjectDetectionTranslatorFactory
        implements Serializable {

    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    @Override
    protected Translator<Image, DetectedObjects> buildBaseTranslator(
            Model model, Map<String, ?> arguments) {
        Translator<Image, DetectedObjects> translator = YoloSegmentationTranslator2.builder(arguments).build();
        return translator;
    }
}
