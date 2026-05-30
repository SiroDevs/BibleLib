const String kAppName = 'BibleLib';
const String kAppCredits = '© Siro Devs';
const String kPackageId = 'com.biblelib';

// Database
const String kDatabaseName = 'biblelib.db';
const int kDatabaseVersion = 1;

// SharedPreferences keys
const String kIsFirstLaunchKey = 'is_first_launch';
const String kSelectedBibleIdsKey = 'selected_bible_ids';
const String kActiveBibleIdKey = 'active_bible_id';
const String kFontSizeKey = 'font_size';
const String kThemeModeKey = 'theme_mode';

// Reader defaults
const double kDefaultFontSize = 16.0;
const double kMinFontSize = 12.0;
const double kMaxFontSize = 32.0;
const String kDefaultBookId = 'GEN';
const String kDefaultChapterId = 'GEN.1';

// API
const String kApiBaseUrl = 'https://rest.api.bible/v1';
const int kApiConnectTimeout = 30000;
const int kApiReceiveTimeout = 30000;

// Batch processing
const int kBatchSize = 10;
const int kMaxConcurrentBatches = 3;
