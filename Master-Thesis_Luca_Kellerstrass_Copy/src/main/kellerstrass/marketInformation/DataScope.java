package kellerstrass.marketInformation;

/**
 * DataScope contains the scope of calibration data we want to use for
 * Calibration. for example the full volatility surface. <br>
 * Has to be combinated with a >>DataSource<<
 * 
 * @author lucak
 *
 */
public enum DataScope {

	RisingTerminals, CoTerminals, ExtendedCoTermindals, FullSurface

}
