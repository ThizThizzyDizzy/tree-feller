/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.thizthizzydizzy.treefeller.lib.com.typesafe.config.impl;

import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigIncluder;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigIncluderClasspath;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigIncluderFile;
import com.thizthizzydizzy.treefeller.lib.com.typesafe.config.ConfigIncluderURL;

interface FullIncluder extends ConfigIncluder, ConfigIncluderFile, ConfigIncluderURL,
            ConfigIncluderClasspath {

}
