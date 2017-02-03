import fecha from 'fecha'

const finnishDateRE = /([0-3]?\d)\.([0-2]?\d)\.(\d\d\d\d)/
export const formatISODate = (date) => date.toISOString().substring(0, 10)
export const parseFinnishDate = (dateStr) => {
  let match = dateStr.match(finnishDateRE)
  if (match) {
    let year = parseInt(match[3], 10)
    let month = parseInt(match[2], 10) - 1
    let day = parseInt(match[1], 10)
    var date = new Date(Date.UTC(year, month, day))
    if (date && date.getDate() === day && date.getMonth() === month) {
      return date
    }
  }
}
export const parseISODateTime = (date) => fecha.parse(date, 'YYYY-MM-DDThh:mm')
export const formatFinnishDateTime = (date) => fecha.format(date, 'D.M.YYYY H:mm')
export const ISO2FinnishDateTime = (date) => formatFinnishDateTime(parseISODateTime(date))

export const parseISODate = (date) => fecha.parse(date, 'YYYY-MM-DD')
export const formatFinnishDate = (date) => fecha.format(date, 'D.M.YYYY')
export const yearFromFinnishDateString = (dateString) => {
  let date = dateString && parseFinnishDate(dateString)
  if(date) {
    return fecha.format(parseFinnishDate(dateString), 'YYYY')
  }
}
export const ISO2FinnishDate = (date) => formatFinnishDate(parseISODate(date))
