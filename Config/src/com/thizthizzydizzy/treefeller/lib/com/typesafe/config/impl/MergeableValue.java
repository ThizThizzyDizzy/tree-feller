package com.thizthizzydizzy.treefeller.lib.com.typesafe.config.impl;

import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigMergeable;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigValue;

interface MergeableValue extends ConfigMergeable {
    // converts a Config to its root object and a ConfigValue to itself
    ConfigValue toFallbackValue();
}
