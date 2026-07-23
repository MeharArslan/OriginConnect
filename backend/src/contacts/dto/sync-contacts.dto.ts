import { IsArray, IsString, ArrayMaxSize } from 'class-validator';
export class SyncContactsDto { @IsArray() @IsString({each:true}) @ArrayMaxSize(1000) phones: string[]; }
