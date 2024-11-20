# Log analyzer
The library parses logs in NGINX format form the given files (in GLOB pattern) or from URL, and counts statistic like requests count,
90-th percentile of the response size, count of different resources requested, response codes, used methods and the number of requests per day.

The filters can be passed to the analyzer to filter the start and the end date of log to parse. 
Furthermore, you can filter the values by patter (e.g. adding **--filter-field** METHOD **--filter-pattern** "G*" would filter only logs where 
method with name starting with "G" are used)

# CLI options
* **--destination**, **--dst**, **-d** | filename to save the result
* **--filter-field**, **-ff** | fields to filter. Default: []<br/>
* **--filter-pattern**, **-fp** | patterns to filter Default: []<br/>
* **--format**, **-o** | output format. Default: markdown. Possible Values: [markdown, adoc]<br/>
* **--from**, **-f** | time in ISO8601 format, logs to be analyzed after which<br/>
* **--help**, **-h** | show help<br/>
* **--source**, **--src**, **-s** | glob/url for log file(-s) to analyze<br/>
* **--to**, **-t** | time in ISO8601 format, logs to be analyzed before which
