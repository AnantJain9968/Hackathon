import { TestBed } from '@angular/core/testing';

import { BarchartDataService } from './barchart-data.service';

describe('BarchartDataService', () => {
  let service: BarchartDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BarchartDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
