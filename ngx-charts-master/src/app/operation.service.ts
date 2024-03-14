import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ChartData } from './chart-data.model';

@Injectable({
  providedIn: 'root'
})
export class OperationService {

  localurl:String;
  constructor(private http:HttpClient) {
    this.localurl="http://localhost:8080";
   }

   getData(){
    return this.http.get<ChartData[]>(this.localurl+'/dashobardData');
   }
}
