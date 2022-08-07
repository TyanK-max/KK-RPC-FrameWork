package github.compress;

import github.extension.SPI;

/**
 * @author TyanK
 * @description: TODO
 * @date 2022/8/7 16:11
 */
@SPI
public interface Compress {
    
    byte[] compress(byte[] bytes);
    
    byte[] deCompress(byte[] bytes);
}
