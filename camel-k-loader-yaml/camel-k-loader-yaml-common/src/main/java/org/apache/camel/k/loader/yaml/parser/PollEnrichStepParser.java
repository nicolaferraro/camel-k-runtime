/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.k.loader.yaml.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.camel.Expression;
import org.apache.camel.k.annotation.yaml.YAMLNodeDefinition;
import org.apache.camel.k.annotation.yaml.YAMLStepParser;
import org.apache.camel.k.loader.yaml.spi.ProcessorStepParser;
import org.apache.camel.model.PollEnrichDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.reifier.PollEnrichReifier;

@YAMLStepParser(id = "poll-enrich", definition = PollEnrichStepParser.Definition.class)
public class PollEnrichStepParser implements ProcessorStepParser {
    @Override
    public ProcessorDefinition<?> toProcessor(Context context) {
        return context.node(Definition.class);
    }

    @YAMLNodeDefinition(reifiers = PollEnrichReifier.class)
    public static final class Definition extends PollEnrichDefinition implements HasExpression {
        @JsonIgnore
        public void setExpression(Expression expression) {
            super.setExpression(expression);
        }

        public void setStrategyRef(String aggregationStrategyRef) {
            super.setAggregationStrategyRef(aggregationStrategyRef);
        }

        public String getStrategyMethodName() {
            return super.getAggregationStrategyRef();
        }
    }
}

