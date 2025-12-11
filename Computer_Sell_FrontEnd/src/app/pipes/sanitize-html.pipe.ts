import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
    name: 'sanitizeHtml'
})
export class SanitizeHtmlPipe implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) { }

    transform(value: string): SafeHtml {
        if (!value) {
            return '';
        }
        return this.sanitizer.sanitize(1, value) || '';
    }
}
