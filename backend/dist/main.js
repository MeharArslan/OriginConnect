"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
require("reflect-metadata");
const core_1 = require("@nestjs/core");
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const app_module_1 = require("./app.module");
const http_exception_filter_1 = require("./common/filters/http-exception.filter");
const compression = require("compression");
const helmet_1 = require("helmet");
async function bootstrap() {
    const logger = new common_1.Logger('Bootstrap');
    const app = await core_1.NestFactory.create(app_module_1.AppModule, { logger: ['log', 'warn', 'error'] });
    const config = app.get(config_1.ConfigService);
    const port = config.get('PORT', 3000);
    app.use((0, helmet_1.default)());
    app.use(compression());
    app.enableCors({ origin: '*', methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'], allowedHeaders: ['Content-Type', 'Authorization'] });
    app.useGlobalPipes(new common_1.ValidationPipe({ whitelist: true, transform: true, forbidNonWhitelisted: false }));
    app.useGlobalFilters(new http_exception_filter_1.AllExceptionsFilter());
    app.setGlobalPrefix('api/v1');
    await app.listen(port);
    logger.log('OriginConnect running on port ' + port);
}
bootstrap();
//# sourceMappingURL=main.js.map